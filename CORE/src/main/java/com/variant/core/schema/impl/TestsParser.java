package com.variant.core.schema.impl;

import static com.variant.core.error.ErrorTemplate.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.variant.core.error.ErrorTemplate;
import com.variant.core.schema.Test;

/**
 * Parse the TESTS clause.
 * @author Igor
 *
 */
public class TestsParser implements Keywords {
	
	/**
	 * @param result
	 * @param viewsObject
	 */
	 static void parseTests(Object testsObject, ParserResponse response) {
		List<Map<String, ?>> rawTests = null;
		try {
			rawTests = (List<Map<String, ?>>) testsObject;
		}
		catch (Exception e) {
			response.addError(INTERNAL, e.getMessage());
		}
		
		if (rawTests.size() == 0) {
			response.addError(PARSER_NO_TESTS);
		}
		
		for (Map<String, ?> rawTest: rawTests) {
			Test test = parseTest(rawTest, response);
			if (test != null && !((SchemaImpl) response.getConfig()).addTest(test)) {
				response.addError(PARSER_TEST_NAME_DUPE, test.getName());
			}
		}
	}
	
	/**
	 * 
	 * @param test
	 * @param response
	 */
	private static Test parseTest(Map<String, ?> test, ParserResponse response) {
		
		String name = null;
		boolean nameFound = false;
		List<TestExperienceImpl> experiences = new ArrayList<TestExperienceImpl>();
		List<TestOnViewImpl> onViews = new ArrayList<TestOnViewImpl>();
		
		// Pass 1: figure out the name.
		for(Map.Entry<String, ?> entry: test.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addError(PARSER_TEST_NAME_NOT_STRING);
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addError(PARSER_TEST_NAME_MISSING);
			}
			return null;
		}
		
		TestImpl result = new TestImpl(name);
		
		// Pass 2: Parse experiences.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(EXPERIENCES)) {
				Object experiencesObject = entry.getValue();
				if (! (experiencesObject instanceof List)) {
					response.addError(PARSER_EXPERIENCES_NOT_LIST, name);
				}
				else {
					List<Object> rawExperiences = (List) experiencesObject;
					if (rawExperiences.size() == 0) {
						response.addError(PARSER_EXPERIENCES_LIST_EMPTY, name);
					}
					else {
						for (Object rawExperience: rawExperiences) {
							TestExperienceImpl experience = parseTestExperience(rawExperience, name, response);
							if (experience != null) {
								experience.setTest(result);
								boolean dupe = false;
								for (TestExperienceImpl e: experiences) {
									if (e.equals(experience))
										response.addError(PARSER_EXPERIENCE_NAME_DUPE, e.getName(), name);
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
					response.addError(PARSER_CONTROL_EXPERIENCE_DUPE, e.getName(), name);
					break;
				}
				else {
					controlExperienceFound = true;
				}
			}
		}
		if (!controlExperienceFound) 
			response.addError(PARSER_IS_CONTROL_MISSING, name);
		
		result.setExperiences(experiences);
		
		// Pass 3: Parse onViews.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME) || entry.getKey().equalsIgnoreCase(EXPERIENCES)) continue;
			
			if (entry.getKey().equalsIgnoreCase(ON_VIEWS)) {
				Object onViewsObject = entry.getValue();
				if (! (onViewsObject instanceof List)) {
					response.addError(PARSER_ONVIEWS_NOT_LIST, name);
				}
				else {
					List<Object> rawOnViews = (List) onViewsObject;
					if (rawOnViews.size() == 0) {
						response.addError(PARSER_ONVIEWS_LIST_EMPTY, name);						
					}
					else {
						for (Object testOnViewObject: rawOnViews) {
							TestOnViewImpl tov = parseTestOnView(testOnViewObject, result, response);
							if (tov != null) onViews.add(tov);
						}
					}
				}
			}
			else {
				response.addError(PARSER_TEST_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}

		if (onViews.isEmpty()) return null;
		
		result.setOnViews(onViews);
		return result;
	}

	
	/**
	 * Parse a test experience
	 * @param rawExperience
	 * @return
	 */
	private static TestExperienceImpl parseTestExperience(Object experienceObject, String testName, ParserResponse response) {
		
		Map<String, ?> experience = null;
		try {
			experience = (Map<String, ?>) experienceObject;
		}
		catch (Exception e) {
			response.addError(PARSER_EXPERIENCE_NOT_OBJECT, testName);
			return null;
		}

		// Pass 1: figure out the name.
		String name = null;
		for (Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME)) {
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addError(PARSER_EXPERIENCE_NAME_NOT_STRING, testName);
					return null;
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}
		
		if (name == null) {
			response.addError(PARSER_TEST_NAME_MISSING);
			return null;
		}
		
		// Pass 2: Finish parsing if we have the name.
		boolean isControl = false;  // default;
		Number weight = null;
		
		for(Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(IS_CONTROL)) {
				try {
					isControl = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_ISCONTROL_NOT_BOOLEAN, testName, name);
				}
			}
			else if (entry.getKey().equalsIgnoreCase(WEIGHT)) {
				try {
					weight = (Number) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_WEIGHT_NOT_NUMBER, testName, name);
					weight = Float.NaN;
				}
			}
			else {
				response.addError(PARSER_EXPERIENCE_UNSUPPORTED_PROPERTY, entry.getKey(), testName, name);
			}
		}
		
		return new TestExperienceImpl(name, weight.floatValue(), isControl);	
	}
	
	/**
	 * Parse a single element of the onViews array.
	 * @param testOnViewObject
	 * @param response
	 * @return
	 */
	private static TestOnViewImpl parseTestOnView(Object testOnViewObject, TestImpl test, ParserResponse response) {
		
		Map<String, Object> rawTestOnView = null;
		
		try {
			rawTestOnView = (Map<String, Object>) testOnViewObject;
		}
		catch (Exception e) {
			response.addError(PARSER_ONVIEW_NOT_OBJECT, test.getName());
			return null;
		}
		
		// Pass 1. Figure out the experienceRef
		String viewRef = null;
		for (Map.Entry<String, Object> entry: rawTestOnView.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(VIEW_REF)) {
				try {
					viewRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_VIEWREF_NOT_STRING, test.getName());
					return null;
				}
			}
		}
		
		if (viewRef == null) {
			response.addError(PARSER_VIEWREF_MISSING, test.getName());
			return null;
		}
		
		// The view must exist.
		ViewImpl refView = (ViewImpl) response.getConfig().getView(viewRef);
		if (refView == null) {
			response.addError(PARSER_VIEWREF_UNDEFINED, viewRef, test.getName());
			return null;
		}

		TestOnViewImpl tov = new TestOnViewImpl(refView, test);

		// Pass 2. Parse the rest of elems.
		List<Object> rawVariants = null;
		
		for (Map.Entry<String, Object> entry: rawTestOnView.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(VIEW_REF)) continue;
			
			if (entry.getKey().equalsIgnoreCase(IS_INVARIANT)) {
				boolean isInvariant = false;
				try {
					isInvariant = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_ISINVARIANT_NOT_BOOLEAN, test.getName(), viewRef);
				}
				tov.setInvariant(isInvariant);
			}
			else if (entry.getKey().equalsIgnoreCase(VARIANTS)) {
				try {
					rawVariants = (List<Object>) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_VARIANTS_NOT_LIST, test.getName(), viewRef);
					return null;
				}
				if (rawVariants.isEmpty()) {
					response.addError(PARSER_VARIANTS_LIST_EMPTY, test.getName(), viewRef);
					return null;					
				}
				for (Object variantObject: rawVariants) {
					TestOnViewVariantImpl variant = parseVariant(variantObject, tov, response);
					if (variant != null) {
						for (Test.OnView.Variant v: tov.getVariants()) {
							if (v.getExperience().equals(variant.getExperience())) {
								response.addError(PARSER_VARIANT_DUPE, v.getExperience().getName(), test.getName(), viewRef);
							}
						}
						tov.addVariant(variant);
					}
				}
			}
		}
		
		// 'isInvariant' is incompatible with 'variants', but one or the other is required.
		if (tov.isInvariant()) {
			if (tov.getVariants().isEmpty()) {
				return tov;
			}
			else {
				response.addError(PARSER_VARIANTS_ISINVARIANT_INCOMPATIBLE, test.getName(), viewRef);
				return null;
			}
		}
		else if (rawVariants == null || rawVariants.size() == 0) {
			response.addError(PARSER_VARIANTS_ISINVARIANT_XOR, test.getName(), viewRef);
			return null;
		}
		
		// Must have a variant for each non-control experience.
		for (Test.Experience e: test.getExperiences()) {
			if (e.isControl()) continue;
			boolean found = false;
			for (Test.OnView.Variant v: tov.getVariants()) {
				if (e.equals(v.getExperience())) {
					found = true;
					break;
				}
			}
			if (!found) {
				response.addError(PARSER_VARIANT_MISSING, e.getName(), test.getName(), viewRef);
			}
		}
		
		return tov;
	}
	
	/**
	 * Parse an element of the list property tests/onViews/variants.
	 * @param variantObject
	 * @param tov
	 * @param response
	 * @return
	 */
	public static TestOnViewVariantImpl parseVariant(Object variantObject, TestOnViewImpl tov, ParserResponse response) {
		
		Map<String, Object> rawVariant = null;
		
		try {
			rawVariant = (Map<String, Object>) variantObject;
		}
		catch (Exception e) {
			response.addError(PARSER_VARIANT_NOT_OBJECT, tov.getTest().getName(), tov.getView().getName());
			return null;
		}
		
		// Pass 1. Find Experience Ref.
		String experienceRef = null;
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(EXPERIENCE_REF)) {
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

		// Pass 2. Parse the rest of experience element.
		String path = null;
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(EXPERIENCE_REF)) continue;
		
			else if (entry.getKey().equalsIgnoreCase(PATH)) {
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
		
		return new TestOnViewVariantImpl(experience, path);
	}
}
