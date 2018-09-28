package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.ALL_PROPER_EXPERIENCES_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_TESTREF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_TEST_DISJOINT;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_CONJOINT_PHANTOM;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_DUPE;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_PROPER_PHANTOM;
import static com.variant.core.schema.parser.error.SemanticError.CONTROL_EXPERIENCE_DUPE;
import static com.variant.core.schema.parser.error.SemanticError.CONTROL_EXPERIENCE_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.DUPE_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.ELEMENT_NOT_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.ELEMENT_NOT_STRING;
import static com.variant.core.schema.parser.error.SemanticError.NAME_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.NAME_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_EMPTY_LIST;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_BOOLEAN;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_NUMBER;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_STRING;
import static com.variant.core.schema.parser.error.SemanticError.PROPER_VARIANT_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.STATEREF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;
import static com.variant.core.schema.parser.error.SemanticError.VARIANTS_ISNONVARIANT_INCOMPATIBLE;
import static com.variant.core.schema.parser.error.SemanticError.VARIANTS_ISNONVARIANT_XOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.UserError.Severity;
import com.variant.core.impl.CoreException;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.schema.impl.VariationExperienceImpl;
import com.variant.core.schema.impl.VariationImpl;
import com.variant.core.schema.impl.VariationOnStateImpl;
import com.variant.core.schema.parser.error.SemanticError.Location;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.MutableInteger;
import com.variant.core.util.StringUtils;

/**
 * Parse the VARIATIONS clause.
 * @author Igor
 *
 */
public class VariationsParser implements Keywords {
	
	private static final Logger LOG = LoggerFactory.getLogger(VariationsParser.class);
	
	/**
	 * @param result
	 * @param viewsObject
	 * @throws VariantRuntimeException 
	 */
	@SuppressWarnings("unchecked")
	static void parse(Object varsObject, Location varsLocation, ParserResponse response, HooksService hooksService) {
		List<Map<String, ?>> rawVars = null;
		try {
			rawVars = (List<Map<String, ?>>) varsObject;
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
		if (rawVars.size() == 0) {
			response.addMessage(varsLocation, PROPERTY_EMPTY_LIST, KEYWORD_VARIATIONS);
		}
		
		int index = 0;
		for (Map<String, ?> rawTest: rawVars) {
			
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

			Location varLocation = varsLocation.plusIx(index++);
			
			// Parse individual test
			Variation var = parseTest(rawTest, varLocation, response);
			if (var != null && !((SchemaImpl) response.getSchema()).addVariation(var)) {
				response.addMessage(varLocation, DUPE_OBJECT, var.getName());
			}
			
			// If no errors, register test scoped hooks.
			if (errorCount.intValue() == 0) {
				for (Hook hook: var.getHooks()) hooksService.initHook(hook, response);

				// Post the test parsed event.
				try {
					hooksService.post(new TestParsedLifecycleEventImpl(var, response));
				}
				catch (Exception e) {
					response.addMessage(ServerError.HOOK_UNHANDLED_EXCEPTION, TestParsedLifecycleEventImpl.class.getName(), e.getMessage());
					LOG.error(ServerError.HOOK_UNHANDLED_EXCEPTION.asMessage(TestParsedLifecycleEventImpl.class.getName(), e.getMessage()), e);
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
	private static Variation parseTest(Map<String, ?> test, Location testLocation, ParserResponse response){
		
		List<VariationExperienceImpl> experiences = new ArrayList<VariationExperienceImpl>();
		List<VariationOnStateImpl> onStates = new ArrayList<VariationOnStateImpl>();

		String name = null;
		boolean nameFound = false;
		
		// Pass 1: Figure out the name.
		for(Map.Entry<String, ?> entry: test.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(testLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
					return null;
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(testLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
						return null;
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(testLocation, NAME_MISSING);
			}
			return null;
		}
		
		VariationImpl result = new VariationImpl(response.getSchema(), name);
		
		// Pass 2: Parse experiences.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCES)) {
				Object experiencesObject = entry.getValue();
				if (! (experiencesObject instanceof List)) {
					response.addMessage(testLocation.plusObj(KEYWORD_EXPERIENCES), PROPERTY_NOT_LIST, KEYWORD_EXPERIENCES);
					return null;
				}
				else {
					List<?> rawExperiences = (List<?>) experiencesObject;
					if (rawExperiences.size() == 0) {
						response.addMessage(testLocation.plusObj(KEYWORD_EXPERIENCES), PROPERTY_EMPTY_LIST, KEYWORD_EXPERIENCES);
						return null; 
					}
					else {
						int index = 0;
						for (Object rawExperience: rawExperiences) {
							Location expLocation = testLocation.plusObj(KEYWORD_EXPERIENCES).plusIx(index++);
							VariationExperienceImpl experience = parseTestExperience(rawExperience, name, expLocation, response);
							if (experience != null) {
								experience.setTest(result);
								for (VariationExperienceImpl e: experiences) {
									if (e.equals(experience)) {
										response.addMessage(expLocation, DUPE_OBJECT, e.getName());
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
		for (VariationExperienceImpl e: experiences) {
			Location expLocation = testLocation.plusObj(KEYWORD_EXPERIENCES).plusIx(expIx++);
			if (e.isControl()) {
				if (controlExperienceFound) {
					response.addMessage(expLocation, CONTROL_EXPERIENCE_DUPE, e.getName(), name);
					break;
				}
				else {
					controlExperienceFound = true;
				}
			}
		}
		if (!controlExperienceFound) 
			response.addMessage(testLocation, CONTROL_EXPERIENCE_MISSING, name);
		
		result.setExperiences(experiences);
		
		
		// Pass 3: Parse conjointVariationRefs, isOn, hooks.
		List<VariationImpl> covarTests = null;
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_CONJOINT_VARIATION_REFS)) {
				Object covarTestRefsObject = entry.getValue();
				if (!(covarTestRefsObject instanceof List)) {
					response.addMessage(testLocation.plusProp(KEYWORD_CONJOINT_VARIATION_REFS), PROPERTY_NOT_LIST, KEYWORD_CONJOINT_VARIATION_REFS);
				}
				else {
					covarTests = new ArrayList<VariationImpl>();
					List<?> rawCovarTestRefs = (List<?>) covarTestRefsObject;
					int refIx = 0;
					for (Object covarTestRefObject: rawCovarTestRefs) {
						Location testRefLocation = testLocation.plusProp(KEYWORD_CONJOINT_VARIATION_REFS).plusIx(refIx++);
						if (!(covarTestRefObject instanceof String)) {
							response.addMessage(testRefLocation, ELEMENT_NOT_STRING, KEYWORD_CONJOINT_VARIATION_REFS);
						}
						else {
							String covarTestRef = (String) covarTestRefObject;
							// Conjoint test, referenced by conjointTestRefs clause must
							// have been initialized by now.  Single pass parser!
							Optional<Variation> conjointVarOpt = response.getSchema().getVariation(covarTestRef);
							if (!conjointVarOpt.isPresent()) {
								response.addMessage(testRefLocation, CONJOINT_TESTREF_UNDEFINED, covarTestRef);
							}
							else {
								covarTests.add((VariationImpl)conjointVarOpt.get());
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
					response.addMessage(testLocation.plusProp(KEYWORD_IS_ON), PROPERTY_NOT_BOOLEAN, KEYWORD_IS_ON);					
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
				HooksParser.parseTestHook(entry.getValue(), result, testLocation.plusObj(KEYWORD_HOOKS), response);
			}

		}
		
		// Resort conjoint tests in ordinal order before adding to the result.
		List<VariationImpl> covarTestsReordered = null;
		if (covarTests != null) {
			covarTestsReordered = new ArrayList<VariationImpl>(covarTests.size());
			for (Variation t: response.getSchema().getVariations()) {
				if (covarTests.contains(t)) covarTestsReordered.add((VariationImpl)t);
			}
		}
		result.setConjointTests(covarTestsReordered);
		
		// Pass 4: Parse onStates, if we have the control variant
		if (controlExperienceFound) {
			for(Map.Entry<String, ?> entry: test.entrySet()) {
				
				if (StringUtils.equalsIgnoreCase(entry.getKey(), 
						KEYWORD_NAME, KEYWORD_EXPERIENCES, KEYWORD_CONJOINT_VARIATION_REFS, KEYWORD_IS_ON, KEYWORD_HOOKS)) continue;
	
				if (entry.getKey().equalsIgnoreCase(KEYWORD_ON_STATES)) {
					
					Location onStatesLocation = testLocation.plusObj(KEYWORD_ON_STATES);
					
					Object onViewsObject = entry.getValue();
					if (! (onViewsObject instanceof List)) {
						response.addMessage(onStatesLocation, PROPERTY_NOT_LIST, KEYWORD_ON_STATES);
					}
					else {
						List<?> rawOnViews = (List<?>) onViewsObject;
						if (rawOnViews.size() == 0) {
							response.addMessage(onStatesLocation, PROPERTY_EMPTY_LIST, KEYWORD_ON_STATES);						
						}
						else {
							int tosIx = 0;
							for (Object testOnViewObject: rawOnViews) {
								Location tosLocation = testLocation.plusObj(KEYWORD_ON_STATES).plusIx(tosIx++);
								VariationOnStateImpl tos = parseTestOnState(testOnViewObject, result, tosLocation, response);
								if (tos != null) {
									boolean dupe = false;
									for (Variation.OnState newTos: onStates) {
										if (tos.getState().equals(newTos.getState())) {
											response.addMessage(tosLocation, DUPE_OBJECT, newTos.getState().getName());
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
					response.addMessage(testLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
				}
			}
		}
		
		if (onStates.isEmpty()) return null;
		
		result.setOnViews(onStates);
		
		// A conjoint test cannot be disjoint.
		if (covarTests != null) {
			int covarTestIx = 0;
			for (Variation covarTest: covarTests) {
				if (result.isSerialWith(covarTest)) {
					Location tosLocation = testLocation.plusProp(KEYWORD_CONJOINT_VARIATION_REFS).plusIx(covarTestIx++);
					response.addMessage(tosLocation, CONJOINT_TEST_DISJOINT, name, covarTest.getName());
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
	private static VariationExperienceImpl parseTestExperience(Object experienceObject, String testName, Location expLocation, ParserResponse response) {
		
		Map<String, ?> experience = null;
		try {
			experience = (Map<String, ?>) experienceObject;
		}
		catch (Exception e) {
			response.addMessage(expLocation, ELEMENT_NOT_OBJECT, KEYWORD_EXPERIENCES);
			return null;
		}

		// Pass 1: figure out the name.
		String name = null;
		for (Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(expLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
					return null;
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(expLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
						return null;
					}
				}
				break;
			}
		}
		
		if (name == null) {
			response.addMessage(expLocation, NAME_MISSING);
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
					response.addMessage(expLocation.plusProp(KEYWORD_IS_CONTROL), PROPERTY_NOT_BOOLEAN, KEYWORD_IS_CONTROL);
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_WEIGHT)) {
				try {
					weight = (Number) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(expLocation.plusProp(KEYWORD_WEIGHT), PROPERTY_NOT_NUMBER, KEYWORD_WEIGHT);
					weight = Float.NaN;
				}
			}
			else {
				response.addMessage(expLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}
		
		return new VariationExperienceImpl(name, weight, isControl);	
	}
	
	/**
	 * Parse a single element of the onStates array.
	 * @param testOnViewObject
	 * @param response
	 * @return
	 * @throws VariantRuntimeException 
	 */
	@SuppressWarnings("unchecked")
	private static VariationOnStateImpl parseTestOnState(Object testOnStateObject, VariationImpl test, Location tosLocation, ParserResponse response) {

		Map<String, Object> rawTestOnState = null;
		
		try {
			rawTestOnState = (Map<String, Object>) testOnStateObject;
		}
		catch (Exception e) {
			response.addMessage(tosLocation, ELEMENT_NOT_OBJECT, KEYWORD_ON_STATES);
			return null;
		}
		
		// Pass 1. Figure out the state.
		String stateRef = null;
		for (Map.Entry<String, Object> entry: rawTestOnState.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(KEYWORD_STATE_REF)) {
				try {
					stateRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(tosLocation.plusProp(KEYWORD_STATE_REF), PROPERTY_NOT_STRING, KEYWORD_STATE_REF);
					return null;
				}
			}
		}
		
		if (stateRef == null) {
			response.addMessage(tosLocation, PROPERTY_MISSING, KEYWORD_STATE_REF);
			return null;
		}
		
		// The state must exist.
		Optional<State> stateOpt = response.getSchema().getState(stateRef);
		if (!stateOpt.isPresent()) {
			response.addMessage(tosLocation.plusProp(KEYWORD_STATE_REF), STATEREF_UNDEFINED, stateRef);
			return null;
		}
		
		VariationOnStateImpl tos = new VariationOnStateImpl((StateImpl)stateOpt.get(), test);

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
					response.addMessage(tosLocation.plusProp(KEYWORD_IS_NONVARIANT), PROPERTY_NOT_BOOLEAN, KEYWORD_IS_NONVARIANT);
				}
				tos.setNonvariant(isNonvariant);
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_VARIANTS)) {
				try {
					rawVariants = (List<Object>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(tosLocation.plusProp(KEYWORD_VARIANTS), PROPERTY_NOT_LIST, KEYWORD_VARIANTS);
					return null;
				}
				if (rawVariants.isEmpty()) {
					response.addMessage(tosLocation.plusProp(KEYWORD_VARIANTS), PROPERTY_EMPTY_LIST, KEYWORD_VARIANTS);
					return null;					
				}
				
				int index = 0;
				for (Object variantObject: rawVariants) {
					
					Location variantLocation = tosLocation.plusObj(KEYWORD_VARIANTS).plusIx(index++);
					
					StateVariantImpl variant = VariantParser.parseVariant(variantObject, variantLocation, tos, response);					
					
					if (variant != null) {
						boolean dupe = false;
						for (StateVariant v: tos.getVariants()) {
							if (v.getExperience().equals(variant.getExperience())) { 
								if (v.isProper() && variant.isProper()) {
									// Dupe proper experience ref and no conjoint experiences in this view.
									response.addMessage(variantLocation, DUPE_OBJECT, v.getExperience().getName());
									dupe = true;
									break;
								}
								else if (!v.isProper() && !variant.isProper() && v.getConjointExperiences().equals(variant.getConjointExperiences())){
									// Dupe local and conjoint list.  Note that for this predicate relies on proper ordering. 
									response.addMessage(variantLocation, CONJOINT_VARIANT_DUPE, StringUtils.join(v.getConjointExperiences(), ", "));
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
				response.addMessage(tosLocation, VARIANTS_ISNONVARIANT_INCOMPATIBLE);
				return null;
			}
		}
		else if (rawVariants == null || rawVariants.size() == 0) {
			response.addMessage(tosLocation, VARIANTS_ISNONVARIANT_XOR);
			return null;
		}
		
		// At least one proper variant required.
		boolean allProperVariantsUndefined = true;
		for (Experience properExperience: test.getExperiences()) {
			if (!properExperience.isPhantom(tos.getState())) { 
				allProperVariantsUndefined = false;
				break;
			}
		}		
		if (allProperVariantsUndefined) {
			response.addMessage(tosLocation.plusObj(KEYWORD_VARIANTS), ALL_PROPER_EXPERIENCES_UNDEFINED);
			return null;
		}
		
		// Confirm we have a variant for each point in the variant space,
		// defined by the proper and conjoint experiences, unless one of them
		// undefined, no matter which one. 
		for (VariantSpace.Point point: tos.variantSpace().getAll()) {
			
			if (point.getVariant() == null && point.isDefinedOn(tos.getState())) {
				
				// We don't have a point and none of the coordinate experiences were declared as undefined.
				if (point.getConjointExperiences().size() == 0) {
					response.addMessage(tosLocation.plusObj(KEYWORD_VARIANTS), PROPER_VARIANT_MISSING, point.getExperience().getName());
				}
				else {
					response.addMessage(
							tosLocation.plusObj(KEYWORD_VARIANTS),
							CONJOINT_VARIANT_MISSING,
							point.getExperience().getName(),
							CollectionsUtils.toString(point.getConjointExperiences(),  ","));
				}
			}
			else if (point.getVariant() != null && !point.isDefinedOn(tos.getState())) {
				// We have a point and one of the coordinate experiences were declared as undefined.
				// Find out which one.
				if (point.getExperience().isPhantom(tos.getState())) {
					response.addMessage(
							tosLocation.plusObj(KEYWORD_VARIANTS),
							CONJOINT_VARIANT_PROPER_PHANTOM, 
							point.getExperience().toString(), tos.getState().getName());
				}
				for (Experience e: point.getConjointExperiences()) {
					if (!e.isPhantom(tos.getState())) continue;
					response.addMessage(
							tosLocation.plusObj(KEYWORD_VARIANTS),
							CONJOINT_VARIANT_CONJOINT_PHANTOM,  
							e.toString(), tos.getState().getName());
				}				
			}
		}
		
		return tos;
	}
	
}
