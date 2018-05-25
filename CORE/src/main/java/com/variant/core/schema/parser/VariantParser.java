package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.schema.impl.TestExperienceImpl;
import com.variant.core.schema.impl.TestImpl;
import com.variant.core.schema.impl.TestOnStateImpl;
import com.variant.core.schema.parser.error.SemanticError.Location;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.StringUtils;

/**
 * Parse the element of the tests/onViews/variants list.
 * 
 * @author Igor
 *
 */
public class VariantParser implements Keywords {

	/**
	 * Parse an element of the list property tests/onViews/variants.
	 * @param variantObject
	 * @param tov
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static StateVariantImpl parseVariant(Object variantObject, Location variantLocation, TestOnStateImpl tov, ParserResponse response) {

		TestImpl test = (TestImpl) tov.getTest();
		StateImpl state = (StateImpl) tov.getState();
		
		Map<String, Object> rawVariant = null;
		
		try {
			rawVariant = (Map<String, Object>) variantObject;
		}
		catch (Exception e) {
			response.addMessage(variantLocation, ELEMENT_NOT_OBJECT, KEYWORD_VARIANTS);
			return null;
		}
		
		// Pass 1. Find proper experienceRef and isPhantom
		// Note that we loop over all entries instead of just getting what we want because
		// the syntax is case insensitive and don't know what to get().
		String experienceRef = null;
		boolean isPhantom = false;
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCE_REF)) {
				try {
					experienceRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(variantLocation.plusProp(KEYWORD_EXPERIENCE_REF), PROPERTY_NOT_STRING, KEYWORD_EXPERIENCE_REF);
					return null;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_PHANTOM)) {
				try {
					isPhantom = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(variantLocation.plusProp(KEYWORD_IS_PHANTOM), PROPERTY_NOT_BOOLEAN, KEYWORD_IS_PHANTOM);
				}
			}
		}
		
		if (experienceRef == null) {
			response.addMessage(variantLocation, PROPERTY_MISSING, KEYWORD_EXPERIENCE_REF);
			return null;
		}
		
		// The experience must exist
		TestExperienceImpl experience = (TestExperienceImpl) test.getExperience(experienceRef);
		if (experience == null) {
			response.addMessage(variantLocation.plusProp(KEYWORD_EXPERIENCE_REF), EXPERIENCEREF_UNDEFINED, experienceRef);
			return null;			
		}

		// Variant cannot refer to a control experience, unless phantom.
		if (experience.isControl() && !isPhantom) {
			response.addMessage(variantLocation.plusProp(KEYWORD_EXPERIENCE_REF), EXPERIENCEREF_ISCONTROL,  experienceRef);
			return null;						
		}
		
		// Pass 2. Find conjointExperienceRefs.
		ArrayList<TestExperienceImpl> conjointTestExperiences = null;
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_CONJOINT_EXPERIENCE_REFS)) {
				
				if (isPhantom) {
					response.addMessage(
							variantLocation.plusProp(KEYWORD_EXPERIENCE_REF),
							PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT, 
							KEYWORD_CONJOINT_EXPERIENCE_REFS);
					
					return null;					
				}
								
				List<?> covarExperienceRefList; 
				try {
					covarExperienceRefList = (List<?>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(
							variantLocation.plusObj(KEYWORD_CONJOINT_EXPERIENCE_REFS),
							PROPERTY_NOT_LIST,  
							KEYWORD_CONJOINT_EXPERIENCE_REFS);
					
					return null;
				}
				
				conjointTestExperiences = new ArrayList<TestExperienceImpl>();

				int index = 0;
				for (Object covarExperienceRefObj: covarExperienceRefList) {
					
					Location covarExpRefLocation = variantLocation.plusObj(KEYWORD_CONJOINT_EXPERIENCE_REFS).plusIx(index++);
					
					Map<String,?> covarExperienceRefMap;
					try {
						covarExperienceRefMap = (Map<String,?>) covarExperienceRefObj;
					}
					catch (Exception e) {
						response.addMessage(
								covarExpRefLocation,
								ELEMENT_NOT_OBJECT, 
								KEYWORD_CONJOINT_EXPERIENCE_REFS);
						
						return null;
					}
					String covarTestRef = null, covarExperienceRef = null;
					try {
						covarTestRef = (String) covarExperienceRefMap.get(KEYWORD_TEST_REF);
					}
					catch (Exception e) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_TEST_REF),
								PROPERTY_NOT_STRING, 
								KEYWORD_TEST_REF);
					}
					try {
						covarExperienceRef = (String) covarExperienceRefMap.get(KEYWORD_EXPERIENCE_REF);
					}
					catch (Exception e) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								PROPERTY_NOT_STRING,  
								KEYWORD_EXPERIENCE_REF);
					}
					
					if (covarTestRef == null || covarExperienceRef == null) return null;
					
					// Covar test must have already been defined.
					TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);					
					if (covarTest == null) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_TEST_REF),
								CONJOINT_EXPERIENCE_TEST_REF_UNDEFINED,
								covarTestRef);
						return null;
					}
					
					// Current view cannot be nonvariant in the covar test.
					if (state.isNonvariantIn(covarTest)) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_EXPERIENCE_TEST_REF_NONVARIANT, 
								covarTestRef, state.getName());
						return null;						
					}
					
					// Covar experience must have already been defined.
					TestExperienceImpl covarExperience = (TestExperienceImpl) covarTest.getExperience(covarExperienceRef);
					if (covarExperience == null) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, 
								covarTestRef, covarExperienceRef);
						return null;
					}

					// This test must declare the other test as conjoint.
					if (test.getConjointTests() == null || !test.getConjointTests().contains(covarTest)) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_VARIANT_TEST_NOT_CONJOINT,
								covarTestRef);
						return null;
					}

					if (conjointTestExperiences.contains(covarExperience)) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_EXPERIENCE_DUPE,
								covarTestRef, covarExperienceRef);
						return null;
					}

					// if multiple conjoint experience refs, they can only reference pairwise conjoint tests.
					for (TestExperienceImpl e: conjointTestExperiences) {
						if (!e.getTest().isConjointWith(covarExperience.getTest())) {
							response.addMessage(
									covarExpRefLocation,
									CONJOINT_EXPERIENCE_REF_TESTS_NOT_CONJOINT, 
									CollectionsUtils.toString(CollectionsUtils.list(e.getTest(), covarExperience.getTest()), ", "));
							return null;
						}
					}

					conjointTestExperiences.add(covarExperience);
				}
			}
		}

		
		// Pass 3. Parse the rest of experience element.
		
		Map<String,String> params = new LinkedHashMap<String, String>();
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (StringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_EXPERIENCE_REF, KEYWORD_IS_PHANTOM, KEYWORD_CONJOINT_EXPERIENCE_REFS)) 
				continue;
		
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				
				if (isPhantom) {
					response.addMessage(
							variantLocation.plusObj(KEYWORD_PARAMETERS),
							PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT, 
							KEYWORD_PARAMETERS);
					return null;					
				}

				params = ParamsParser.parse(entry.getValue(), variantLocation.plusObj(KEYWORD_PARAMETERS), response);
			}
			else {
				response.addMessage(variantLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}

		// Don't create a state variant if undefined.
		if (isPhantom) {
			experience.addUninstrumentedState(state);
			return null;
		}
		
		// Resort covarTestExperiences in ordinal order, if present.
		List<TestExperienceImpl> orderedCovarTestExperiences = null; 
		if (conjointTestExperiences != null) {
			
			orderedCovarTestExperiences = new ArrayList<TestExperienceImpl>(conjointTestExperiences.size());

			for (Test t: response.getSchema().getTests()) {
				for (Experience e: conjointTestExperiences) {
					if (t.equals(e.getTest())) {
						orderedCovarTestExperiences.add((TestExperienceImpl) e);
						break;
					}
				}
			}
		}
		
		return new StateVariantImpl(tov, experience, orderedCovarTestExperiences, params);
	}
}
