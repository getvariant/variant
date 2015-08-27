package com.variant.core.schema.impl;

import static com.variant.core.error.ErrorTemplate.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.util.VariantStringUtils;

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
	public static TestOnViewVariantImpl parseVariant(Object variantObject, TestOnViewImpl tov, ParserResponseImpl response) 
	throws VariantRuntimeException {
		
		Map<String, Object> rawVariant = null;
		
		try {
			rawVariant = (Map<String, Object>) variantObject;
		}
		catch (Exception e) {
			response.addError(PARSER_VARIANT_NOT_OBJECT, tov.getTest().getName(), tov.getView().getName());
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
					response.addError(PARSER_EXPERIENCEREF_NOT_STRING, tov.getTest().getName(), tov.getView().getName());
					return null;
				}
			}
		}
		
		if (experienceRef == null) {
			response.addError(PARSER_EXPERIENCEREF_MISSING, tov.getTest().getName(), tov.getView().getName());
			return null;
		}
		
		// The experience must exist
		TestExperienceImpl experience = (TestExperienceImpl) tov.getTest().getExperience(experienceRef);
		if (experience == null) {
			response.addError(PARSER_EXPERIENCEREF_UNDEFINED, experienceRef, tov.getTest().getName(), tov.getView().getName());
			return null;			
		}

		// Variant cannot refer to a control experience
		if (experience.isControl) {
			response.addError(PARSER_EXPERIENCEREF_ISCONTROL, experienceRef, tov.getTest().getName(), tov.getView().getName());
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
					response.addError(PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST, tov.getTest().getName(), tov.getView().getName(), experienceRef);
					return null;
				}
				for (Object covarExperienceRefObj: covarExperienceRefList) {
					Map<String,?> covarExperienceRefMap;
					try {
						covarExperienceRefMap = (Map<String,?>) covarExperienceRefObj;
					}
					catch (Exception e) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT, tov.getTest().getName(), tov.getView().getName(), experienceRef);
						return null;
					}
					String covarTestRef = null, covarExperienceRef = null;
					try {
						covarTestRef = (String) covarExperienceRefMap.get(KEYWORD_TEST_REF);
					}
					catch (Exception e) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING, tov.getTest().getName(), tov.getView().getName(), experienceRef);
					}
					try {
						covarExperienceRef = (String) covarExperienceRefMap.get(KEYWORD_EXPERIENCE_REF);
					}
					catch (Exception e) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING, tov.getTest().getName(), tov.getView().getName(), experienceRef);
					}
					
					if (covarTestRef == null || covarExperienceRef == null) return null;
					
					// Covar test must have already been defined.
					TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);					
					if (covarTest == null) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED, covarTestRef, tov.getTest().getName(), tov.getView().getName(), experienceRef);
						return null;
					}
					
					// Current view cannot be nonvariant in the covar test.
					if (tov.getView().isNonvariantIn(covarTest)) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT, covarTestRef, tov.getTest().getName(), tov.getView().getName(), experienceRef);
						return null;						
					}
					
					// Covar experience must have already been defined.
					TestExperienceImpl covarExperience = (TestExperienceImpl) covarTest.getExperience(covarExperienceRef);
					if (covarExperience == null) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, covarTestRef, covarExperienceRef, tov.getTest().getName(), tov.getView().getName(), experienceRef);
						return null;
					}

					// This test must declare the other test as covariant.
					if (!((TestImpl)tov.getTest()).getCovariantTests().contains(covarTest)) {
						response.addError(PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT, covarTestRef, covarExperienceRef, tov.getTest().getName(), tov.getView().getName());
						return null;
					}

					if (covarTestExperiences.contains(covarExperience)) {
						response.addError(PARSER_COVARIANT_EXPERIENCE_DUPE, covarTestRef, covarExperienceRef, tov.getTest().getName(), tov.getView().getName(), experienceRef);
						return null;
					}
	
					covarTestExperiences.add(covarExperience);
				}
				
			}

		}

		
		// Pass 3. Parse the rest of experience element.
		String path = null;
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_EXPERIENCE_REF, KEYWORD_COVARIANT_EXPERIENCE_REFS)) continue;
		
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PATH)) {
				try {
					path = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_EXPERIENCEREF_PATH_NOT_STRING, tov.getTest().getName(), tov.getView().getName(), experienceRef);
				}
			}
			else {
				response.addError(PARSER_VARIANTS_UNSUPPORTED_PROPERTY, entry.getKey(), tov.getTest().getName(), tov.getView().getName());
			}
		}
		
		if (path == null) return null;
		
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
		return new TestOnViewVariantImpl(tov, experience, orderedCovarTestExperiences, path);
	}
}
