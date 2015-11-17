package com.variant.core.schema.impl;

import static com.variant.core.schema.parser.MessageTemplate.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.util.VariantStringUtils;

/**
 * Parse the TESTS clause.
 * @author Igor
 *
 */
public class TestsParser implements Keywords {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestsParser.class);
	
	/**
	 * @param result
	 * @param viewsObject
	 * @throws VariantRuntimeException 
	 */
	@SuppressWarnings("unchecked")
	static void parseTests(Object testsObject, ParserResponseImpl response) throws VariantRuntimeException {
		List<Map<String, ?>> rawTests = null;
		try {
			rawTests = (List<Map<String, ?>>) testsObject;
		}
		catch (Exception e) {
			ParserMessage err = response.addMessage(INTERNAL, e.getMessage());
			LOG.error(err.getMessage(), e);
		}
		
		if (rawTests.size() == 0) {
			response.addMessage(PARSER_NO_TESTS);
		}
		
		for (Map<String, ?> rawTest: rawTests) {
			Test test = parseTest(rawTest, response);
			if (test != null && !((SchemaImpl) response.getSchema()).addTest(test)) {
				response.addMessage(PARSER_TEST_NAME_DUPE, test.getName());
			}
		}
	}
	
	/**
	 * 
	 * @param test
	 * @param response
	 * @throws VariantRuntimeException 
	 */
	private static Test parseTest(Map<String, ?> test, ParserResponseImpl response) throws VariantRuntimeException {
		
		List<TestImpl> covarTests = new ArrayList<TestImpl>();
		List<TestExperienceImpl> experiences = new ArrayList<TestExperienceImpl>();
		List<TestOnStateImpl> onViews = new ArrayList<TestOnStateImpl>();

		String name = null;
		boolean nameFound = false;
		
		// Pass 1: Figure out the name.
		for(Map.Entry<String, ?> entry: test.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(PARSER_TEST_NAME_NOT_STRING);
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(PARSER_TEST_NAME_MISSING);
			}
			return null;
		}
		
		TestImpl result = new TestImpl(name);
		
		// Pass 2: Parse experiences.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCES)) {
				Object experiencesObject = entry.getValue();
				if (! (experiencesObject instanceof List)) {
					response.addMessage(PARSER_EXPERIENCES_NOT_LIST, name);
				}
				else {
					List<?> rawExperiences = (List<?>) experiencesObject;
					if (rawExperiences.size() == 0) {
						response.addMessage(PARSER_EXPERIENCES_LIST_EMPTY, name);
					}
					else {
						for (Object rawExperience: rawExperiences) {
							TestExperienceImpl experience = parseTestExperience(rawExperience, name, response);
							if (experience != null) {
								experience.setTest(result);
								for (TestExperienceImpl e: experiences) {
									if (e.equals(experience)) {
										response.addMessage(PARSER_EXPERIENCE_NAME_DUPE, e.getName(), name);
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
		for (TestExperienceImpl e: experiences) {
			if (e.isControl) {
				if (controlExperienceFound) {
					response.addMessage(PARSER_CONTROL_EXPERIENCE_DUPE, e.getName(), name);
					break;
				}
				else {
					controlExperienceFound = true;
				}
			}
		}
		if (!controlExperienceFound) 
			response.addMessage(PARSER_IS_CONTROL_MISSING, name);
		
		result.setExperiences(experiences);
		
		
		// Pass 3: Parse covariantTestRefs, isOn and idleDaysToLive
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_COVARIANT_TEST_REFS)) {
				Object covarTestRefsObject = entry.getValue();
				if (!(covarTestRefsObject instanceof List)) {
					response.addMessage(PARSER_COVARIANT_TESTS_NOT_LIST, name);
				}
				else {
					List<?> rawCovarTestRefs = (List<?>) covarTestRefsObject;
					for (Object covarTestRefObject: rawCovarTestRefs) {
						if (!(covarTestRefObject instanceof String)) {
							response.addMessage(PARSER_COVARIANT_TESTREF_NOT_STRING, name);
						}
						else {
							String covarTestRef = (String) covarTestRefObject;
							// Covariant test, referenced by covariantTestRefs clause must
							// have been initialized by now.  Single pass parser!
							TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);
							if (covarTest == null) {
								response.addMessage(PARSER_COVARIANT_TESTREF_UNDEFINED, covarTestRef, name);
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
					response.addMessage(PARSER_TEST_ISON_NOT_BOOLEAN, name);					
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_IDLE_DAYS_TO_LIVE)) {
				try {
					Integer days = (Integer) entry.getValue();
					if (days < 0) {
						response.addMessage(PARSER_TEST_IDLE_DAYS_TO_LIVE_NEGATIVE, name);
					}
					else {
						result.setIdleDaysToLive(days);
					}
				}
				catch (Exception e)  {
					response.addMessage(PARSER_TEST_IDLE_DAYS_TO_LIVE_NOT_INT, name);					
				}
			}
		}
		
		// Resort covariant tests in ordinal order before adding to the result.
		List<TestImpl> covarTestsReordered = new ArrayList<TestImpl>(covarTests.size());
		for (Test t: response.getSchema().getTests()) {
			if (covarTests.contains(t)) covarTestsReordered.add((TestImpl)t);
		}
		result.setCovariantTests(covarTestsReordered);
		
		// Pass 4: Parse onViews.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_NAME, KEYWORD_EXPERIENCES, KEYWORD_COVARIANT_TEST_REFS, KEYWORD_IS_ON, KEYWORD_IDLE_DAYS_TO_LIVE)) continue;

			if (entry.getKey().equalsIgnoreCase(KEYWORD_ON_STATES)) {
				Object onViewsObject = entry.getValue();
				if (! (onViewsObject instanceof List)) {
					response.addMessage(PARSER_ONSTATES_NOT_LIST, name);
				}
				else {
					List<?> rawOnViews = (List<?>) onViewsObject;
					if (rawOnViews.size() == 0) {
						response.addMessage(PARSER_ONSTATES_LIST_EMPTY, name);						
					}
					else {
						for (Object testOnViewObject: rawOnViews) {
							TestOnStateImpl tov = parseTestOnView(testOnViewObject, result, response);
							if (tov != null) {
								boolean dupe = false;
								for (Test.OnState newTov: onViews) {
									if (tov.getState().equals(newTov.getState())) {
										response.addMessage(PARSER_STATEREF_DUPE, newTov.getState().getName(), name);
										dupe = true;
										break;
									}
								}

								if (!dupe) onViews.add(tov);
							}
						}
					}
				}
			}
			else {
				response.addMessage(PARSER_TEST_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}

		if (onViews.isEmpty()) return null;
		
		result.setOnViews(onViews);
		
		// A covariant test cannot be disjoint.
		for (Test covarTest: covarTests) {
			if (result.isDisjointWith(covarTest)) {
				response.addMessage(PARSER_COVARIANT_TEST_DISJOINT, covarTest.getName(), name);
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
	private static TestExperienceImpl parseTestExperience(Object experienceObject, String testName, ParserResponseImpl response) {
		
		Map<String, ?> experience = null;
		try {
			experience = (Map<String, ?>) experienceObject;
		}
		catch (Exception e) {
			response.addMessage(PARSER_EXPERIENCE_NOT_OBJECT, testName);
			return null;
		}

		// Pass 1: figure out the name.
		String name = null;
		for (Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(PARSER_EXPERIENCE_NAME_NOT_STRING, testName);
					return null;
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}
		
		if (name == null) {
			response.addMessage(PARSER_TEST_NAME_MISSING);
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
					response.addMessage(PARSER_ISCONTROL_NOT_BOOLEAN, testName, name);
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_WEIGHT)) {
				try {
					weight = (Number) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_WEIGHT_NOT_NUMBER, testName, name);
					weight = Float.NaN;
				}
			}
			else {
				response.addMessage(PARSER_EXPERIENCE_UNSUPPORTED_PROPERTY, entry.getKey(), testName, name);
			}
		}
		
		return new TestExperienceImpl(name, weight, isControl);	
	}
	
	/**
	 * Parse a single element of the onViews array.
	 * @param testOnViewObject
	 * @param response
	 * @return
	 * @throws VariantRuntimeException 
	 */
	@SuppressWarnings("unchecked")
	private static TestOnStateImpl parseTestOnView(Object testOnViewObject, TestImpl test, ParserResponseImpl response) throws VariantRuntimeException {
		
		Map<String, Object> rawTestOnView = null;
		
		try {
			rawTestOnView = (Map<String, Object>) testOnViewObject;
		}
		catch (Exception e) {
			response.addMessage(PARSER_ONSTATES_NOT_OBJECT, test.getName());
			return null;
		}
		
		// Pass 1. Figure out the experienceRef
		String stateRef = null;
		for (Map.Entry<String, Object> entry: rawTestOnView.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(KEYWORD_STATE_REF)) {
				try {
					stateRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_STATEREF_NOT_STRING, test.getName());
					return null;
				}
			}
		}
		
		if (stateRef == null) {
			response.addMessage(PARSER_STATEREF_MISSING, test.getName());
			return null;
		}
		
		// The state must exist.
		StateImpl refState = (StateImpl) response.getSchema().getState(stateRef);
		if (refState == null) {
			response.addMessage(PARSER_STATEREF_UNDEFINED, stateRef, test.getName());
			return null;
		}
		
		TestOnStateImpl tov = new TestOnStateImpl(refState, test);

		// Pass 2. Parse the rest of elems.
		List<Object> rawVariants = null;
		
		for (Map.Entry<String, Object> entry: rawTestOnView.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(KEYWORD_STATE_REF)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_NONVARIANT)) {
				boolean isNonvariant = false;
				try {
					isNonvariant = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_ISNONVARIANT_NOT_BOOLEAN, test.getName(), stateRef);
				}
				tov.setNonvariant(isNonvariant);
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_VARIANTS)) {
				try {
					rawVariants = (List<Object>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_VARIANTS_NOT_LIST, test.getName(), stateRef);
					return null;
				}
				if (rawVariants.isEmpty()) {
					response.addMessage(PARSER_VARIANTS_LIST_EMPTY, test.getName(), stateRef);
					return null;					
				}
				for (Object variantObject: rawVariants) {
					TestOnViewVariantImpl variant = VariantParser.parseVariant(variantObject, tov, response);
					if (variant != null) {
						boolean dupe = false;
						for (Test.OnState.Variant v: tov.getVariants()) {
							if (v.getExperience().equals(variant.getExperience())) { 
								if (v.getCovariantExperiences().isEmpty() && variant.getCovariantExperiences().isEmpty()) {
									// Dupe local experience ref and no covariant experiences in this view.
									response.addMessage(
											PARSER_VARIANT_DUPE, 
											v.getExperience().getName(), 
											test.getName(), stateRef);
									dupe = true;
									break;
								}
								else if (v.getCovariantExperiences().equals(variant.getCovariantExperiences())){
									// Dupe local and covariant list.  Note that for this predicate relies on proper ordering. 
									response.addMessage(
											PARSER_COVARIANT_VARIANT_DUPE,  
											StringUtils.join(v.getCovariantExperiences(), ", "), 
											test.getName(), stateRef, v.getExperience().getName());
									dupe = true;
									break;
								}
							}
						}
					    // Don't add a dupe.
						if (!dupe) {
							tov.addVariant(variant);
						}
					}
				}
			}
		}
		
		// 'isNonvariant' is incompatible with 'variants', but one or the other is required.
		if (tov.isNonvariant()) {
			if (tov.getVariants().isEmpty()) {
				return tov;
			}
			else {
				response.addMessage(PARSER_VARIANTS_ISNONVARIANT_INCOMPATIBLE, test.getName(), stateRef);
				return null;
			}
		}
		else if (rawVariants == null || rawVariants.size() == 0) {
			response.addMessage(PARSER_VARIANTS_ISNONVARIANT_XOR, test.getName(), stateRef);
			return null;
		}
		
		// Confirm Must have a variant for each vector in the variant space
		// defined by the local and covariant experiences.
		for (VariantSpace.Point point: tov.variantSpace().getAll()) {
			if (point.getVariant() == null) {
				if (point.getCovariantExperiences().size() == 0) {
					response.addMessage(PARSER_VARIANT_MISSING, point.getExperience().getName(), test.getName(), stateRef);
				}
				else {
					response.addMessage(
							PARSER_COVARIANT_VARIANT_MISSING, 
							VariantStringUtils.toString(point.getCovariantExperiences(),  ","), test.getName(), stateRef, point.getExperience().getName());
				}
			}
		}
		
		return tov;
	}
	
}
