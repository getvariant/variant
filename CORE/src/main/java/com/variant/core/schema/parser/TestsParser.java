package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.variant.core.CommonError;
import com.variant.core.CoreException;
import com.variant.core.UserError.Severity;
import com.variant.core.VariantException;
import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.schema.impl.TestExperienceImpl;
import com.variant.core.schema.impl.TestImpl;
import com.variant.core.schema.impl.TestOnStateImpl;
import com.variant.core.schema.parser.error.CollateralMessage;
import com.variant.core.util.MutableInteger;
import com.variant.core.util.VariantStringUtils;

/**
 * Parse the TESTS clause.
 * @author Igor
 *
 */
public class TestsParser implements Keywords {
	
	//private static final Logger LOG = LoggerFactory.getLogger(TestsParser.class);
	
	/**
	 * @param result
	 * @param viewsObject
	 * @throws VariantRuntimeException 
	 */
	@SuppressWarnings("unchecked")
	static void parse(Object testsObject, Location testsLocation, ParserResponse response, HooksService hooksService) {
		List<Map<String, ?>> rawTests = null;
		try {
			rawTests = (List<Map<String, ?>>) testsObject;
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
		if (rawTests.size() == 0) {
			response.addMessage(NO_TESTS, testsLocation);
		}
		
		int index = 0;
		for (Map<String, ?> rawTest: rawTests) {
			
			// Increment a local integer count whenever a parse error occurs.
			final MutableInteger errorCount = new MutableInteger(0);
			response.setMessageListener(
					new ParserResponse.MessageListener() {
						@Override
						public void messageAdded(ParserMessage message) {
							if (message.getSeverity().greaterOrEqual(Severity.ERROR)) 
								errorCount.add(1);
						}
			});

			Location testLocation = testsLocation.plus(index++);
			
			// Parse individual test
			Test test = parseTest(rawTest, testLocation, response);
			if (test != null && !((SchemaImpl) response.getSchema()).addTest(test)) {
				response.addMessage(TEST_NAME_DUPE, testLocation, test.getName());
			}
			
			// If no errors, register test scoped hooks.
			if (errorCount.intValue() == 0) {
				for (Hook hook: test.getHooks()) hooksService.initHook(hook, response);

				// Post the test parsed event.
				try {
					hooksService.post(new TestParsedLifecycleEventImpl(test, response));
				}
				catch (VariantException e) {
					response.addMessage(CollateralMessage.HOOK_UNHANDLED_EXCEPTION, TestParsedLifecycleEventImpl.class.getName(), e.getMessage());
					throw e;
				}
			}
			response.setMessageListener(null);

		}
	}
	
	/**
	 * 
	 * @param test
	 * @param response
	 * @throws VariantRuntimeException 
	 */
	private static Test parseTest(Map<String, ?> test, Location testLocation, ParserResponse response){
		
		List<TestExperienceImpl> experiences = new ArrayList<TestExperienceImpl>();
		List<TestOnStateImpl> onStates = new ArrayList<TestOnStateImpl>();

		String name = null;
		boolean nameFound = false;
		
		// Pass 1: Figure out the name.
		for(Map.Entry<String, ?> entry: test.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(TEST_NAME_INVALID, testLocation.plus(KEYWORD_NAME));
					return null;
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(TEST_NAME_INVALID, testLocation.plus(KEYWORD_NAME));
						return null;
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(TEST_NAME_MISSING, testLocation);
			}
			return null;
		}
		
		TestImpl result = new TestImpl(response.getSchema(), name);
		
		// Pass 2: Parse experiences.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCES)) {
				Object experiencesObject = entry.getValue();
				if (! (experiencesObject instanceof List)) {
					response.addMessage(EXPERIENCES_NOT_LIST, testLocation.plus(KEYWORD_EXPERIENCES), name);
					return null;
				}
				else {
					List<?> rawExperiences = (List<?>) experiencesObject;
					if (rawExperiences.size() == 0) {
						response.addMessage(EXPERIENCES_LIST_EMPTY, testLocation.plus(KEYWORD_EXPERIENCES), name);
						return null; 
					}
					else {
						int index = 0;
						for (Object rawExperience: rawExperiences) {
							Location expLocation = testLocation.plus(KEYWORD_EXPERIENCES).plus(index++);
							TestExperienceImpl experience = parseTestExperience(rawExperience, name, expLocation, response);
							if (experience != null) {
								experience.setTest(result);
								for (TestExperienceImpl e: experiences) {
									if (e.equals(experience)) {
										response.addMessage(EXPERIENCE_NAME_DUPE, expLocation, e.getName(), name);
										break;
									}
								}
								experiences.add(experience);
							}
						}
					}
				}
			}
		}
		
		// One must be control.
		boolean controlExperienceFound = false;
		int expIx = 0;
		for (TestExperienceImpl e: experiences) {
			Location expLocation = testLocation.plus(KEYWORD_EXPERIENCES).plus(expIx++);
			if (e.isControl()) {
				if (controlExperienceFound) {
					response.addMessage(CONTROL_EXPERIENCE_DUPE, expLocation, e.getName(), name);
					break;
				}
				else {
					controlExperienceFound = true;
				}
			}
		}
		if (!controlExperienceFound) 
			response.addMessage(CONTROL_EXPERIENCE_MISSING, testLocation.plus(KEYWORD_EXPERIENCES), name);
		
		result.setExperiences(experiences);
		
		
		// Pass 3: Parse covariantTestRefs, isOn, hooks.
		List<TestImpl> covarTests = null;
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_COVARIANT_TEST_REFS)) {
				Object covarTestRefsObject = entry.getValue();
				if (!(covarTestRefsObject instanceof List)) {
					response.addMessage(COVARIANT_TESTS_NOT_LIST, testLocation.plus(KEYWORD_COVARIANT_TEST_REFS), name);
				}
				else {
					covarTests = new ArrayList<TestImpl>();
					List<?> rawCovarTestRefs = (List<?>) covarTestRefsObject;
					int refIx = 0;
					for (Object covarTestRefObject: rawCovarTestRefs) {
						Location testRefLocation = testLocation.plus(KEYWORD_COVARIANT_TEST_REFS).plus(refIx++);
						if (!(covarTestRefObject instanceof String)) {
							response.addMessage(COVARIANT_TESTREF_NOT_STRING, testRefLocation, name);
						}
						else {
							String covarTestRef = (String) covarTestRefObject;
							// Covariant test, referenced by covariantTestRefs clause must
							// have been initialized by now.  Single pass parser!
							TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);
							if (covarTest == null) {
								response.addMessage(COVARIANT_TESTREF_UNDEFINED, testRefLocation, covarTestRef, name);
							}
							else {
								covarTests.add(covarTest);
							}
						}
					}
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_ON)) {
				try {
					Boolean isOn = (Boolean) entry.getValue();
					result.setIsOn(isOn);
				}
				catch (Exception e)  {
					response.addMessage(TEST_ISON_NOT_BOOLEAN, testLocation.plus(KEYWORD_IS_ON), name);					
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
				HooksParser.parseTestHook(entry.getValue(), result, testLocation.plus(KEYWORD_HOOKS), response);
			}

		}
		
		// Resort covariant tests in ordinal order before adding to the result.
		List<TestImpl> covarTestsReordered = null;
		if (covarTests != null) {
			covarTestsReordered = new ArrayList<TestImpl>(covarTests.size());
			for (Test t: response.getSchema().getTests()) {
				if (covarTests.contains(t)) covarTestsReordered.add((TestImpl)t);
			}
		}
		result.setCovariantTests(covarTestsReordered);
		
		// Pass 4: Parse onStates, if we have the control variant
		if (controlExperienceFound) {
			for(Map.Entry<String, ?> entry: test.entrySet()) {
				
				if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), 
						KEYWORD_NAME, KEYWORD_EXPERIENCES, KEYWORD_COVARIANT_TEST_REFS, KEYWORD_IS_ON, KEYWORD_HOOKS)) continue;
	
				if (entry.getKey().equalsIgnoreCase(KEYWORD_ON_STATES)) {
					
					Location onStatesLocation = testLocation.plus(KEYWORD_ON_STATES);
					
					Object onViewsObject = entry.getValue();
					if (! (onViewsObject instanceof List)) {
						response.addMessage(ONSTATES_NOT_LIST, onStatesLocation, name);
					}
					else {
						List<?> rawOnViews = (List<?>) onViewsObject;
						if (rawOnViews.size() == 0) {
							response.addMessage(ONSTATES_LIST_EMPTY, onStatesLocation, name);						
						}
						else {
							int tosIx = 0;
							for (Object testOnViewObject: rawOnViews) {
								Location tosLocation = testLocation.plus(KEYWORD_ON_STATES).plus(tosIx++);
								TestOnStateImpl tos = parseTestOnState(testOnViewObject, result, tosLocation, response);
								if (tos != null) {
									boolean dupe = false;
									for (Test.OnState newTos: onStates) {
										if (tos.getState().equals(newTos.getState())) {
											response.addMessage(STATEREF_DUPE, tosLocation, newTos.getState().getName(), name);
											dupe = true;
											break;
										}
									}
	
									if (!dupe) onStates.add(tos);
								}
							}
						}
					}
				}
				else {
					response.addMessage(TEST_UNSUPPORTED_PROPERTY, testLocation, entry.getKey(), name);
				}
			}
		}
		
		if (onStates.isEmpty()) return null;
		
		result.setOnViews(onStates);
		
		// A covariant test cannot be disjoint.
		if (covarTests != null) {
			int covarTestIx = 0;
			for (Test covarTest: covarTests) {
				covarTestIx++;
				if (result.isSerialWith(covarTest)) {
					Location tosLocation = testLocation.plus(KEYWORD_COVARIANT_TEST_REFS).plus(covarTestIx);
					response.addMessage(COVARIANT_TEST_DISJOINT, tosLocation, covarTest.getName(), name);
				}
			}
		}
		
		return result;
	}

	
	/**
	 * Parse a test experience
	 * @param rawExperience
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static TestExperienceImpl parseTestExperience(Object experienceObject, String testName, Location expLocation, ParserResponse response) {
		
		Map<String, ?> experience = null;
		try {
			experience = (Map<String, ?>) experienceObject;
		}
		catch (Exception e) {
			response.addMessage(EXPERIENCE_NOT_OBJECT, expLocation, testName);
			return null;
		}

		// Pass 1: figure out the name.
		String name = null;
		for (Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(EXPERIENCE_NAME_INVALID, expLocation.plus(KEYWORD_NAME), testName);
					return null;
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(EXPERIENCE_NAME_INVALID, expLocation.plus(KEYWORD_NAME), testName);
						return null;
					}
				}
				break;
			}
		}
		
		if (name == null) {
			response.addMessage(EXPERIENCE_NAME_MISSING, expLocation, testName);
			return null;
		}

		// Pass 2: Finish parsing if we have the name.
		boolean isControl = false;  // default;
		Number weight = null;
		
		for(Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_CONTROL)) {
				try {
					isControl = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(ISCONTROL_NOT_BOOLEAN, expLocation.plus(KEYWORD_IS_CONTROL), testName, name);
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_WEIGHT)) {
				try {
					weight = (Number) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(WEIGHT_NOT_NUMBER, expLocation.plus(KEYWORD_WEIGHT), testName, name);
					weight = Float.NaN;
				}
			}
			else {
				response.addMessage(EXPERIENCE_UNSUPPORTED_PROPERTY, expLocation, entry.getKey(), testName, name);
			}
		}
		
		return new TestExperienceImpl(name, weight, isControl);	
	}
	
	/**
	 * Parse a single element of the onStates array.
	 * @param testOnViewObject
	 * @param response
	 * @return
	 * @throws VariantRuntimeException 
	 */
	@SuppressWarnings("unchecked")
	private static TestOnStateImpl parseTestOnState(Object testOnStateObject, TestImpl test, Location tosLocation, ParserResponse response) {
		
		Map<String, Object> rawTestOnState = null;
		
		try {
			rawTestOnState = (Map<String, Object>) testOnStateObject;
		}
		catch (Exception e) {
			response.addMessage(ONSTATES_NOT_OBJECT, tosLocation, test.getName());
			return null;
		}
		
		// Pass 1. Figure out the experienceRef
		String stateRef = null;
		for (Map.Entry<String, Object> entry: rawTestOnState.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(KEYWORD_STATE_REF)) {
				try {
					stateRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(STATEREF_NOT_STRING, tosLocation.plus(KEYWORD_STATE_REF), test.getName());
					return null;
				}
			}
		}
		
		if (stateRef == null) {
			response.addMessage(STATEREF_MISSING, tosLocation, test.getName());
			return null;
		}
		
		// The state must exist.
		StateImpl refState = (StateImpl) response.getSchema().getState(stateRef);
		if (refState == null) {
			response.addMessage(STATEREF_UNDEFINED, tosLocation.plus(KEYWORD_STATE_REF), stateRef, test.getName());
			return null;
		}
		
		TestOnStateImpl tos = new TestOnStateImpl(refState, test);

		// Pass 2. Parse the rest of elems.
		List<Object> rawVariants = null;
		
		for (Map.Entry<String, Object> entry: rawTestOnState.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_STATE_REF)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_NONVARIANT)) {
				boolean isNonvariant = false;
				try {
					isNonvariant = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(ISNONVARIANT_NOT_BOOLEAN, tosLocation.plus(KEYWORD_IS_NONVARIANT), test.getName(), stateRef);
				}
				tos.setNonvariant(isNonvariant);
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_VARIANTS)) {
				try {
					rawVariants = (List<Object>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(VARIANTS_NOT_LIST, tosLocation.plus(KEYWORD_VARIANTS), test.getName(), stateRef);
					return null;
				}
				if (rawVariants.isEmpty()) {
					response.addMessage(VARIANTS_LIST_EMPTY, tosLocation.plus(KEYWORD_VARIANTS), test.getName(), stateRef);
					return null;					
				}
				
				int index = 0;
				for (Object variantObject: rawVariants) {
					
					StateVariantImpl variant = VariantParser.parseVariant(variantObject, tos, response);					
					index++;
					
					if (variant != null) {
						boolean dupe = false;
						for (StateVariant v: tos.getVariants()) {
							if (v.getExperience().equals(variant.getExperience())) { 
								if (v.isProper() && variant.isProper()) {
									// Dupe proper experience ref and no covariant experiences in this view.
									response.addMessage(
											VARIANT_DUPE,
											tosLocation.plus(KEYWORD_VARIANTS).plus(index),
											v.getExperience().getName(), 
											test.getName(), stateRef);
									dupe = true;
									break;
								}
								else if (!v.isProper() && !variant.isProper() && v.getCovariantExperiences().equals(variant.getCovariantExperiences())){
									// Dupe local and covariant list.  Note that for this predicate relies on proper ordering. 
									response.addMessage(
											COVARIANT_VARIANT_DUPE, 
											tosLocation.plus(KEYWORD_VARIANTS).plus(index),
											StringUtils.join(v.getCovariantExperiences(), ", "), 
											test.getName(), stateRef, v.getExperience().getName());
									dupe = true;
									break;
								}
							}
						}
					    // Don't add a dupe.
						if (!dupe) {
							tos.addVariant(variant);
						}
					}
				}
			}
		}
		
		// 'isNonvariant' is incompatible with 'variants', but one or the other is required.
		if (tos.isNonvariant()) {
			if (tos.getVariants().isEmpty()) {
				return tos;
			}
			else {
				response.addMessage(VARIANTS_ISNONVARIANT_INCOMPATIBLE, tosLocation.plus(KEYWORD_IS_NONVARIANT), test.getName(), stateRef);
				return null;
			}
		}
		else if (rawVariants == null || rawVariants.size() == 0) {
			response.addMessage(VARIANTS_ISNONVARIANT_XOR, tosLocation.plus(KEYWORD_IS_NONVARIANT), test.getName(), stateRef);
			return null;
		}
		
		// At least one proper variant required to be defined.
		boolean allProperVariantsUndefined = true;
		for (Experience properExperience: test.getExperiences()) {
			if (properExperience.isDefinedOn(tos.getState())) { 
				allProperVariantsUndefined = false;
				break;
			}
		}		
		if (allProperVariantsUndefined) {
			response.addMessage(
					ALL_PROPER_EXPERIENCES_UNDEFINED, 
					tosLocation.plus(KEYWORD_VARIANTS),
					test.getName(), stateRef);
			return null;
		}
		
		// Confirm Must have a variant for each vector in the variant space,
		// defined by the proper and covariant experiences, unless one of them
		// undefined, no matter which one. 
		for (VariantSpace.Point point: tos.variantSpace().getAll()) {
			
			if (point.getVariant() == null && point.isDefinedOn(tos.getState())) {
				
				// We don't have a point and none of the coordinate experiences were declared as undefined.
				if (point.getCovariantExperiences().size() == 0) {
					response.addMessage(
							VARIANT_MISSING, 
							tosLocation.plus(KEYWORD_VARIANTS), 
							point.getExperience().getName(), test.getName(), stateRef);
				}
				else {
					response.addMessage(
							COVARIANT_VARIANT_MISSING,
							tosLocation.plus(KEYWORD_VARIANTS),
							point.getExperience().getName(),
							VariantStringUtils.toString(point.getCovariantExperiences(),  ","), 
							test.getName(), stateRef);
				}
			}
			else if (point.getVariant() != null && !point.isDefinedOn(tos.getState())) {
				// We have a point and one of the coordinate experiences were declared as undefined.
				// Find out which one.
				if (!point.getExperience().isDefinedOn(tos.getState())) {
					response.addMessage(
							COVARIANT_VARIANT_PROPER_UNDEFINED, 
							tosLocation.plus(KEYWORD_VARIANTS),
							point.getExperience().getName(),
							VariantStringUtils.toString(point.getCovariantExperiences(),  ","), 
							test.getName(), stateRef);
				}
				for (Experience e: point.getCovariantExperiences()) {
					if (e.isDefinedOn(tos.getState())) continue;
					response.addMessage(
							COVARIANT_VARIANT_COVARIANT_UNDEFINED, 
							tosLocation.plus(KEYWORD_VARIANTS),
							point.getExperience().getName(),
							VariantStringUtils.toString(point.getCovariantExperiences(),  ","), 
							e.toString(), test.getName(), stateRef);
				}				
			}
		}
		
		return tos;
	}
	
}
