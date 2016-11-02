package com.variant.server.schema;

import static com.variant.core.exception.Error.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.util.CaseInsensitiveMap;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;
import com.variant.core.xdm.impl.Keywords;
import com.variant.core.xdm.impl.StateImpl;
import com.variant.core.xdm.impl.StateVariantImpl;
import com.variant.core.xdm.impl.TestExperienceImpl;
import com.variant.core.xdm.impl.TestImpl;
import com.variant.core.xdm.impl.TestOnStateImpl;

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
	public static StateVariantImpl parseVariant(Object variantObject, TestOnStateImpl tov, ParserResponseImpl response) 
	throws VariantRuntimeException {
		
		TestImpl test = (TestImpl) tov.getTest();
		StateImpl state = (StateImpl) tov.getState();
		
		Map<String, Object> rawVariant = null;
		
		try {
			rawVariant = (Map<String, Object>) variantObject;
		}
		catch (Exception e) {
			response.addMessage(PARSER_VARIANT_NOT_OBJECT, test.getName(), state.getName());
			return null;
		}
		
		// Pass 1. Find proper experienceRef and isDefined
		// Note that we loop over all entries instead of just getting what we want because
		// the syntax is case insensitive and don't know what to get().
		String experienceRef = null;
		boolean isDefined = true;
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCE_REF)) {
				try {
					experienceRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_EXPERIENCEREF_NOT_STRING, test.getName(), state.getName());
					return null;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_DEFINED)) {
				try {
					isDefined = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_ISDEFINED_NOT_BOOLEAN, test.getName(), state.getName());
				}
			}
		}
		
		if (experienceRef == null) {
			response.addMessage(PARSER_EXPERIENCEREF_MISSING, test.getName(), state.getName());
			return null;
		}
		
		// The experience must exist
		TestExperienceImpl experience = (TestExperienceImpl) test.getExperience(experienceRef);
		if (experience == null) {
			response.addMessage(PARSER_EXPERIENCEREF_UNDEFINED, experienceRef, test.getName(), state.getName());
			return null;			
		}

		// Variant cannot refer to a control experience, unless undefined.
		if (experience.isControl() && isDefined) {
			response.addMessage(PARSER_EXPERIENCEREF_ISCONTROL, experienceRef, test.getName(), state.getName());
			return null;						
		}
		
		// Pass 2. Find covariantExperienceRefs.
		ArrayList<TestExperienceImpl> covarTestExperiences = null;
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_COVARIANT_EXPERIENCE_REFS)) {
				
				if (!isDefined) {
					response.addMessage(PARSER_COVARIANT_EXPERIENCEREFS_NOT_ALLOWED, test.getName(), state.getName(), experienceRef);
					return null;					
				}
								
				List<?> covarExperienceRefList; 
				try {
					covarExperienceRefList = (List<?>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST, test.getName(), state.getName(), experienceRef);
					return null;
				}
				
				covarTestExperiences = new ArrayList<TestExperienceImpl>();

				for (Object covarExperienceRefObj: covarExperienceRefList) {
					Map<String,?> covarExperienceRefMap;
					try {
						covarExperienceRefMap = (Map<String,?>) covarExperienceRefObj;
					}
					catch (Exception e) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT, test.getName(), state.getName(), experienceRef);
						return null;
					}
					String covarTestRef = null, covarExperienceRef = null;
					try {
						covarTestRef = (String) covarExperienceRefMap.get(KEYWORD_TEST_REF);
					}
					catch (Exception e) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING, test.getName(), state.getName(), experienceRef);
					}
					try {
						covarExperienceRef = (String) covarExperienceRefMap.get(KEYWORD_EXPERIENCE_REF);
					}
					catch (Exception e) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING, test.getName(), state.getName(), experienceRef);
					}
					
					if (covarTestRef == null || covarExperienceRef == null) return null;
					
					// Covar test must have already been defined.
					TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);					
					if (covarTest == null) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED, covarTestRef, test.getName(), state.getName(), experienceRef);
						return null;
					}
					
					// Current view cannot be nonvariant in the covar test.
					if (state.isNonvariantIn(covarTest)) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT, covarTestRef, test.getName(), state.getName(), experienceRef);
						return null;						
					}
					
					// Covar experience must have already been defined.
					TestExperienceImpl covarExperience = (TestExperienceImpl) covarTest.getExperience(covarExperienceRef);
					if (covarExperience == null) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, covarTestRef, covarExperienceRef, test.getName(), state.getName(), experienceRef);
						return null;
					}

					// This test must declare the other test as covariant.
					if (test.getCovariantTests() == null || !test.getCovariantTests().contains(covarTest)) {
						response.addMessage(PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT, covarTestRef, covarExperienceRef, test.getName(), state.getName());
						return null;
					}

					if (covarTestExperiences.contains(covarExperience)) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_DUPE, covarTestRef, covarExperienceRef, test.getName(), state.getName(), experienceRef);
						return null;
					}

					// if multiple covarint experience refs, they can only reference pairwise covariant tests.
					for (TestExperienceImpl e: covarTestExperiences) {
						if (!e.getTest().isCovariantWith(covarExperience.getTest())) {
							response.addMessage(
									PARSER_COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT, 
									test.getName(), 
									tov.getState().getName(), 
									experienceRef,
									VariantStringUtils.toString(VariantCollectionsUtils.list(e, covarExperience), ","));
							return null;
						}
					}

					covarTestExperiences.add(covarExperience);
				}
			}
		}

		
		// Pass 3. Parse the rest of experience element.
		Map<String,String> params = null;
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_EXPERIENCE_REF, KEYWORD_IS_DEFINED, KEYWORD_COVARIANT_EXPERIENCE_REFS)) 
				continue;
		
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				
				if (!isDefined) {
					response.addMessage(PARSER_EXPERIENCEREF_PARAMS_NOT_ALLOWED, test.getName(), tov.getState().getName(), experienceRef);
					return null;					
				}

				try {
					params = (Map<String,String>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_EXPERIENCEREF_PARAMS_NOT_OBJECT, test.getName(), tov.getState().getName(), experienceRef);
				}
			}
			else {
				response.addMessage(PARSER_VARIANTS_UNSUPPORTED_PROPERTY, entry.getKey(), test.getName(), state.getName());
			}
		}

		// Don't create a state variant if undefined.
		if (!isDefined) {
			experience.addUninstrumentedState(state);
			return null;
		}

		// The map from json parser is not case insensitive.
		CaseInsensitiveMap<String> ciParams = new CaseInsensitiveMap<String>();
		if (params != null) ciParams.putAll(params);  
		
		// Resort covarTestExperiences in ordinal order, if present.
		List<TestExperienceImpl> orderedCovarTestExperiences = null; 
		if (covarTestExperiences != null) {
			
			orderedCovarTestExperiences = new ArrayList<TestExperienceImpl>(covarTestExperiences.size());

			for (Test t: response.getSchema().getTests()) {
				for (Experience e: covarTestExperiences) {
					if (t.equals(e.getTest())) {
						orderedCovarTestExperiences.add((TestExperienceImpl) e);
						break;
					}
				}
			}
		}
		
		return new StateVariantImpl(tov, experience, orderedCovarTestExperiences, ciParams);
	}
}
