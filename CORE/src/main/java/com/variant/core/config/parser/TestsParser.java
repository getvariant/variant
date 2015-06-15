package com.variant.core.config.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.variant.core.config.Test;
import com.variant.core.config.View;

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
			response.addError(ParserErrorTemplate.INTERNAL, e.getMessage());
		}
		
		if (rawTests.size() == 0) {
			response.addError(ParserErrorTemplate.NO_TESTS);
		}
		
		for (Map<String, ?> rawTest: rawTests) {
			Test test = parseTest(rawTest, response);
			if (test != null && !((ConfigImpl) response.getConfig()).addTest(test)) {
				response.addError(ParserErrorTemplate.TEST_NAME_DUPE, test.getName());
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
					response.addError(ParserErrorTemplate.TEST_NAME_NOT_STRING);
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addError(ParserErrorTemplate.TEST_NAME_MISSING);
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
					response.addError(ParserErrorTemplate.EXPERIENCES_NOT_LIST, name);
				}
				else {
					List<Object> rawExperiences = (List) experiencesObject;
					if (rawExperiences.size() == 0) {
						response.addError(ParserErrorTemplate.EXPERIENCES_LIST_EMPTY, name);
					}
					else {
						for (Object rawExperience: rawExperiences) {
							TestExperienceImpl experience = parseTestExperience(rawExperience, name, response);
							if (experience != null) {
								experience.setTest(result);
								experiences.add(experience);
							}
						}
					}
				}
			}
		}
		
		result.setExperiences(experiences);
		
		// Pass 3: Parse onViews.
		for(Map.Entry<String, ?> entry: test.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME) || entry.getKey().equalsIgnoreCase(EXPERIENCES)) continue;
			
			if (entry.getKey().equalsIgnoreCase(ON_VIEWS)) {
				Object onViewsObject = entry.getValue();
				if (! (onViewsObject instanceof List)) {
					response.addError(ParserErrorTemplate.ONVIEWS_NOT_LIST, name);
				}
				else {
					List<Object> rawOnViews = (List) onViewsObject;
					if (rawOnViews.size() == 0) {
						response.addError(ParserErrorTemplate.ONVIEWS_LIST_EMPTY, name);						
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
				response.addError(ParserErrorTemplate.TEST_UNSUPPORTED_PROPERTY, entry.getKey(), name);
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
			response.addError(ParserErrorTemplate.EXPERIENCE_NOT_OBJECT, testName);
			return null;
		}

		// Pass 1: figure out the name.
		String name = null;
		for (Map.Entry<String, ?> entry: experience.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME)) {
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addError(ParserErrorTemplate.TEST_NAME_NOT_STRING, testName);
					return null;
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}
		
		if (name == null) {
			response.addError(ParserErrorTemplate.TEST_NAME_MISSING);
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
					response.addError(ParserErrorTemplate.ISCONTROL_NOT_BOOLEAN, testName, name);
				}
			}
			else if (entry.getKey().equalsIgnoreCase(WEIGHT)) {
				try {
					weight = (Number) entry.getValue();
				}
				catch (Exception e) {
					response.addError(ParserErrorTemplate.WEIGHT_NOT_BOOLEAN, testName, name);
				}
			}
			else {
				response.addError(ParserErrorTemplate.EXPERIENCE_UNSUPPORTED_PROPERTY, entry.getKey(), testName, name);
			}
		}
		
		return weight == null ? null : new TestExperienceImpl(name, weight.floatValue(), isControl);	
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
			response.addError(ParserErrorTemplate.ONVIEW_NOT_OBJECT, test.getName());
		}
		
		// Pass 1. Figure out the experienceRef
		String viewRef = null;
		for (Map.Entry<String, Object> entry: rawTestOnView.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(VIEW_REF)) {
				try {
					viewRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addError(ParserErrorTemplate.VIEWREF_NOT_STRING, test.getName());
					return null;
				}
			}
		}
		
		if (viewRef == null) {
			response.addError(ParserErrorTemplate.VIEWREF_NOT_STRING, test.getName());
			return null;
		}
		
		// The view must exist.
		ViewImpl refView = (ViewImpl) response.getConfig().getView(viewRef);
		if (refView == null) {
			response.addError(ParserErrorTemplate.VIEWREF_INVALID, viewRef, test.getName());
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
					response.addError(ParserErrorTemplate.ISINVARIANT_NOT_BOOLEAN, test.getName(), viewRef);
				}
				tov.setInvariant(isInvariant);
			}
			else if (entry.getKey().equalsIgnoreCase(VARIANTS)) {
				try {
					rawVariants = (List<Object>) entry.getValue();
				}
				catch (Exception e) {
					response.addError(ParserErrorTemplate.VARIANTS_NOT_LIST, test.getName(), viewRef);
					return null;
				}
				if (rawVariants.isEmpty()) {
					response.addError(ParserErrorTemplate.VARIANTS_LIST_EMPTY, test.getName(), viewRef);
					return null;					
				}
				for (Object variantObject: rawVariants) {
					TestOnViewVariantImpl variant = parseVariant(variantObject, tov, response);
					if (variant != null) tov.addVariant(variant);
				}
			}
		}
		
		// 'isInvariant' is incompatible with 'invariantss', but one or the other is required.
		if (tov.isInvariant()) {
			if (tov.getVariants().isEmpty()) {
				return tov;
			}
			else {
				response.addError(ParserErrorTemplate.VARIANTS_ISINVARIANT_INCOMPATIBLE, test.getName(), viewRef);
				return null;
			}
		}
		else {
			if (rawTestOnView.entrySet().isEmpty()) {
				response.addError(ParserErrorTemplate.VARIANTS_ISINVARIANT_XOR, test.getName(), viewRef);
				return null;
			}
			else {
				return tov;
			}
		}			
		
	}
	
	/**
	 * Parse an element of the 'variants' list inside the 'onViews' element.
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
			response.addError(ParserErrorTemplate.VARIANT_NOT_OBJECT, tov.getView().getName(), tov.getTest().getName());
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
					response.addError(ParserErrorTemplate.EXPERIENCEREF_NOT_STRING, tov.getTest().getName(), tov.getView().getName());
					return null;
				}
			}
		}
		
		if (experienceRef == null) {
			response.addError(ParserErrorTemplate.EXPERIENCEREF_MISSING, tov.getTest().getName(), tov.getView().getName());
			return null;
		}
		
		// The experience must exist
		TestExperienceImpl experience = (TestExperienceImpl) tov.getTest().getExperience(experienceRef);
		if (experience == null) {
			response.addError(ParserErrorTemplate.EXPERIENCEREF_UNDEFINED, experienceRef, tov.getTest().getName(), tov.getView().getName());
			return null;			
		}

		// Variant cannot refer to a control experience
		if (experience.isControl) {
			response.addError(ParserErrorTemplate.EXPERIENCEREF_ISCONTROL, experienceRef, tov.getTest().getName(), tov.getView().getName());
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
					response.addError(ParserErrorTemplate.EXPERIENCEREF_PATH_NOT_STRING, tov.getTest().getName(), tov.getView().getName());
				}
			}
			else {
				response.addError(ParserErrorTemplate.VARIANTS_UNSUPPORTED_PROPERTY, entry.getKey(), tov.getTest().getName(), tov.getView().getName());
			}
		}
		
		if (path == null) return null;
		
		return new TestOnViewVariantImpl(experience, path);
	}
}
