package com.variant.core.schema.impl;

import static com.variant.core.error.ErrorTemplate.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.variant.core.schema.Test;
import com.variant.core.util.VariantStringUtils;

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
	@SuppressWarnings("unchecked")
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
			if (test != null && !((SchemaImpl) response.getSchema()).addTest(test)) {
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
		
		List<TestImpl> covarTests = new ArrayList<TestImpl>();
		List<TestExperienceImpl> experiences = new ArrayList<TestExperienceImpl>();
		List<TestOnViewImpl> onViews = new ArrayList<TestOnViewImpl>();

		String name = null;
		boolean nameFound = false;
		
		// Pass 1: Figure out the name.
		for(Map.Entry<String, ?> entry: test.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
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
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCES)) {
				Object experiencesObject = entry.getValue();
				if (! (experiencesObject instanceof List)) {
					response.addError(PARSER_EXPERIENCES_NOT_LIST, name);
				}
				else {
					List<?> rawExperiences = (List<?>) experiencesObject;
					if (rawExperiences.size() == 0) {
						response.addError(PARSER_EXPERIENCES_LIST_EMPTY, name);
					}
					else {
						for (Object rawExperience: rawExperiences) {
							TestExperienceImpl experience = parseTestExperience(rawExperience, name, response);
							if (experience != null) {
								experience.setTest(result);
								for (TestExperienceImpl e: experiences) {
									if (e.equals(experience)) {
										response.addError(PARSER_EXPERIENCE_NAME_DUPE, e.getName(), name);
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
		
		// Pass 3: Parse covariantTestRefs.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_COVARIANT_TEST_REFS)) {
				Object covarTestRefsObject = entry.getValue();
				if (!(covarTestRefsObject instanceof List)) {
					response.addError(PARSER_COVARIANT_TESTS_NOT_LIST, name);
				}
				else {
					List<?> rawCovarTestRefs = (List<?>) covarTestRefsObject;
					for (Object covarTestRefObject: rawCovarTestRefs) {
						if (!(covarTestRefObject instanceof String)) {
							response.addError(PARSER_COVARIANT_TESTREF_NOT_STRING, name);
						}
						else {
							String covarTestRef = (String) covarTestRefObject;
							// Covariant test, referenced by covariantTestRefs clause must
							// have been initialized by now.  Single pass parser!
							TestImpl covarTest = (TestImpl) response.getSchema().getTest(covarTestRef);
							if (covarTest == null) {
								response.addError(PARSER_COVARIANT_TESTREF_UNDEFINED, covarTestRef, name);
							}
							else {
								covarTests.add(covarTest);
							}
						}
					}
				}
			}
		}
		
		result.setCovariantTests(covarTests);

		// Pass 4: Parse onViews.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_NAME, KEYWORD_EXPERIENCES, KEYWORD_COVARIANT_TEST_REFS)) continue;

			if (entry.getKey().equalsIgnoreCase(KEYWORD_ON_VIEWS)) {
				Object onViewsObject = entry.getValue();
				if (! (onViewsObject instanceof List)) {
					response.addError(PARSER_ONVIEWS_NOT_LIST, name);
				}
				else {
					List<?> rawOnViews = (List<?>) onViewsObject;
					if (rawOnViews.size() == 0) {
						response.addError(PARSER_ONVIEWS_LIST_EMPTY, name);						
					}
					else {
						for (Object testOnViewObject: rawOnViews) {
							TestOnViewImpl tov = parseTestOnView(testOnViewObject, result, response);
							if (tov != null) {
								boolean dupe = false;
								for (Test.OnView newTov: onViews) {
									if (tov.getView().equals(newTov.getView())) {
										response.addError(PARSER_VIEWREF_DUPE, newTov.getView().getName(), name);
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
	@SuppressWarnings("unchecked")
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
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
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
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_CONTROL)) {
				try {
					isControl = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_ISCONTROL_NOT_BOOLEAN, testName, name);
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_WEIGHT)) {
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
	@SuppressWarnings("unchecked")
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
			if (entry.getKey().equalsIgnoreCase(KEYWORD_VIEW_REF)) {
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
		ViewImpl refView = (ViewImpl) response.getSchema().getView(viewRef);
		if (refView == null) {
			response.addError(PARSER_VIEWREF_UNDEFINED, viewRef, test.getName());
			return null;
		}
		
		TestOnViewImpl tov = new TestOnViewImpl(refView, test);

		// Pass 2. Parse the rest of elems.
		List<Object> rawVariants = null;
		
		for (Map.Entry<String, Object> entry: rawTestOnView.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(KEYWORD_VIEW_REF)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_INVARIANT)) {
				boolean isInvariant = false;
				try {
					isInvariant = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addError(PARSER_ISINVARIANT_NOT_BOOLEAN, test.getName(), viewRef);
				}
				tov.setInvariant(isInvariant);
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_VARIANTS)) {
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
					TestOnViewVariantImpl variant = VariantParser.parseVariant(variantObject, tov, response);
					if (variant != null) {
						for (Test.OnView.Variant v: tov.getVariants()) {
							if (v.getLocalExperience().equals(variant.getLocalExperience())) {
								response.addError(PARSER_VARIANT_DUPE, v.getLocalExperience().getName(), test.getName(), viewRef);
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
		
		// Must have a variant for each local (in this test) non-control experience.
		for (Test.Experience e: test.getExperiences()) {
			if (e.isControl()) continue;
			boolean found = false;
			for (Test.OnView.Variant v: tov.getVariants()) {
				if (e.equals(v.getLocalExperience())) {
					found = true;
					break;
				}
			}
			if (!found) {
				response.addError(PARSER_VARIANT_MISSING, e.getName(), test.getName(), viewRef);
			}
		}

		// Must have a variant for each covariant test's non-control experience,
		// unless the remote variant is invariant.
		for (Test covarTest: test.getDeclaredCovariantTests()) {
			if (refView.isInvariantIn(covarTest)) continue;
			for (Test.Experience covarExperience: covarTest.getExperiences()) {
				if (covarExperience.isControl()) continue;
				boolean found = false;
				for (Test.OnView.Variant v: tov.getVariants()) {
					if (covarExperience.equals(v.getLocalExperience())) {
						found = true;
						break;
					}
				}
				if (!found) {
					response.addError(
							PARSER_COVARIANT_VARIANT_MISSING, 
							covarExperience.getTest().getName(), covarExperience.getName(), test.getName(), viewRef);
				}
			}
		}
		
		return tov;
	}
	
}
