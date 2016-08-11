package com.variant.core.xdm.impl;

import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_DUPE;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_ISCONTROL;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_MISSING;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_NOT_STRING;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_PARAMS_NOT_OBJECT;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_UNDEFINED;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_VARIANTS_UNSUPPORTED_PROPERTY;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_VARIANT_NOT_OBJECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.variant.core.event.impl.util.CaseInsensitiveMap;
import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

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
		
		Map<String, Object> rawVariant = null;
		
		try {
			rawVariant = (Map<String, Object>) variantObject;
		}
		catch (Exception e) {
			response.addMessage(PARSER_VARIANT_NOT_OBJECT, tov.getTest().getName(), tov.getState().getName());
			return null;
		}
		
		// Pass 1. Find local experienceRef
		String experienceRef = null;
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCE_REF)) {
				try {
					experienceRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_EXPERIENCEREF_NOT_STRING, tov.getTest().getName(), tov.getState().getName());
					return null;
				}
			}
		}
		
		if (experienceRef == null) {
			response.addMessage(PARSER_EXPERIENCEREF_MISSING, tov.getTest().getName(), tov.getState().getName());
			return null;
		}
		
		// The experience must exist
		TestExperienceImpl experience = (TestExperienceImpl) tov.getTest().getExperience(experienceRef);
		if (experience == null) {
			response.addMessage(PARSER_EXPERIENCEREF_UNDEFINED, experienceRef, tov.getTest().getName(), tov.getState().getName());
			return null;			
		}

		// Variant cannot refer to a control experience
		if (experience.isControl) {
			response.addMessage(PARSER_EXPERIENCEREF_ISCONTROL, experienceRef, tov.getTest().getName(), tov.getState().getName());
			return null;						
		}
		
		// Pass 2. Find covariantExperienceRefs.
		ArrayList<TestExperienceImpl> covarTestExperiences = new ArrayList<TestExperienceImpl>();
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_COVARIANT_EXPERIENCE_REFS)) {
				List<?> covarExperienceRefList; 
				try {
					covarExperienceRefList = (List<?>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST, tov.getTest().getName(), tov.getState().getName(), experienceRef);
					return null;
				}
				for (Object covarExperienceRefObj: covarExperienceRefList) {
					Map<String,?> covarExperienceRefMap;
					try {
						covarExperienceRefMap = (Map<String,?>) covarExperienceRefObj;
					}
					catch (Exception e) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT, tov.getTest().getName(), tov.getState().getName(), experienceRef);
						return null;
					}
					String covarTestRef = null, covarExperienceRef = null;
					try {
						covarTestRef = (String) covarExperienceRefMap.get(KEYWORD_TEST_REF);
					}
					catch (Exception e) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING, tov.getTest().getName(), tov.getState().getName(), experienceRef);
					}
					try {
						covarExperienceRef = (String) covarExperienceRefMap.get(KEYWORD_EXPERIENCE_REF);
					}
					catch (Exception e) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING, tov.getTest().getName(), tov.getState().getName(), experienceRef);
					}
					
					if (covarTestRef == null || covarExperienceRef == null) return null;
					
					// Covar test must have already been defined.
					TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);					
					if (covarTest == null) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED, covarTestRef, tov.getTest().getName(), tov.getState().getName(), experienceRef);
						return null;
					}
					
					// Current view cannot be nonvariant in the covar test.
					if (tov.getState().isNonvariantIn(covarTest)) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT, covarTestRef, tov.getTest().getName(), tov.getState().getName(), experienceRef);
						return null;						
					}
					
					// Covar experience must have already been defined.
					TestExperienceImpl covarExperience = (TestExperienceImpl) covarTest.getExperience(covarExperienceRef);
					if (covarExperience == null) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, covarTestRef, covarExperienceRef, tov.getTest().getName(), tov.getState().getName(), experienceRef);
						return null;
					}

					// This test must declare the other test as covariant.
					if (!((TestImpl)tov.getTest()).getCovariantTests().contains(covarTest)) {
						response.addMessage(PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT, covarTestRef, covarExperienceRef, tov.getTest().getName(), tov.getState().getName());
						return null;
					}

					if (covarTestExperiences.contains(covarExperience)) {
						response.addMessage(PARSER_COVARIANT_EXPERIENCE_DUPE, covarTestRef, covarExperienceRef, tov.getTest().getName(), tov.getState().getName(), experienceRef);
						return null;
					}

					// if multiple covarint experience refs, they can only reference pairwise covariant tests.
					for (TestExperienceImpl e: covarTestExperiences) {
						if (!e.getTest().isCovariantWith(covarExperience.getTest())) {
							response.addMessage(
									PARSER_COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT, 
									tov.getTest().getName(), 
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
			
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_EXPERIENCE_REF, KEYWORD_COVARIANT_EXPERIENCE_REFS)) continue;
		
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				try {
					params = (Map<String,String>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(PARSER_EXPERIENCEREF_PARAMS_NOT_OBJECT, tov.getTest().getName(), tov.getState().getName(), experienceRef);
				}
			}
			else {
				response.addMessage(PARSER_VARIANTS_UNSUPPORTED_PROPERTY, entry.getKey(), tov.getTest().getName(), tov.getState().getName());
			}
		}
		
		// If params were not supplied, simply inherit state's params.
		// Otherwise override them.
		if (params == null) {
			params = tov.getState().getParameterMap();
		}
		else {
			params = new CaseInsensitiveMap<String>(params);  // The map from json parser is not case insensitive.
			params = (Map<String,String>) VariantCollectionsUtils.mapMerge(tov.getState().getParameterMap(), params);
		}
		
		// Resort covarTestExperiences in ordinal order
		List<TestExperienceImpl> orderedCovarTestExperiences = new ArrayList<TestExperienceImpl>(covarTestExperiences.size());

		for (Test t: response.getSchema().getTests()) {
			for (Experience e: covarTestExperiences) {
				if (t.equals(e.getTest())) {
					orderedCovarTestExperiences.add((TestExperienceImpl) e);
					break;
				}
			}
		}
		return new StateVariantImpl(tov, experience, orderedCovarTestExperiences, params);
	}
}
