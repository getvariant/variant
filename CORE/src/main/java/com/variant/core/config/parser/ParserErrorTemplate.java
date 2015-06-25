package com.variant.core.config.parser;

/**
 * The immutable, uncontextualized part of the parser error.
 * 
 * @author Igor
 */
public enum ParserErrorTemplate {
	
	// View related errors
	NO_VIEWS_CLAUSE                    (ParserError.Severity.INFO, "VIEWS clause is not found"),                                                            /// junit exists.
	NO_VIEWS                           (ParserError.Severity.INFO, "No views found"),                                                                       ///
	VIEW_NAME_MISSING                  (ParserError.Severity.ERROR, "View name is missing"),                                                                ///
	VIEW_NAME_NOT_STRING               (ParserError.Severity.ERROR, "View name must be a string"),                                                          ///
	VIEW_NAME_DUPE                     (ParserError.Severity.ERROR, "Duplicate view name [%s]"),                                                            ///
	VIEW_PATH_MISSING                  (ParserError.Severity.ERROR, "'view/path' property is missing (View [%s])"),                                         ///
	VIEW_PATH_NOT_STRING               (ParserError.Severity.ERROR, "'view/path' property must be a string (View [%s])"),                                   ///
	VIEW_UNSUPPORTED_PROPERTY          (ParserError.Severity.WARN,  "Unsupported property 'view/%s' (View [%s])"),                                          ///
	
	// Test related errors
	NO_TESTS_CLAUSE                    (ParserError.Severity.INFO, "'tests' clause is missing"),                                                            ///
	NO_TESTS                           (ParserError.Severity.INFO, "No tests found"),                                                                       ///
	TEST_NAME_MISSING                  (ParserError.Severity.ERROR, "Test name is missing"),                                                                ///
	TEST_NAME_NOT_STRING               (ParserError.Severity.ERROR, "Test name must be a string"),                                                          ///
	TEST_NAME_DUPE                     (ParserError.Severity.ERROR, "Duplicate test name [%s]"),                                                            ///
	TEST_UNSUPPORTED_PROPERTY          (ParserError.Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"),                                         ///
	EXPERIENCES_NOT_LIST               (ParserError.Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"),                             ///
	EXPERIENCES_LIST_EMPTY             (ParserError.Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"),              ///
	EXPERIENCE_NOT_OBJECT              (ParserError.Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])"),                      /// 
	EXPERIENCE_NAME_NOT_STRING         (ParserError.Severity.ERROR, "'tests/experience/name' property must be a string (Test [%s])"),                       ///
	ISCONTROL_NOT_BOOLEAN              (ParserError.Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"),///
	WEIGHT_NOT_NUMBER                  (ParserError.Severity.ERROR, "'tests/experience/weight' property must be a number (Test [%s], Experience [%s])"),    ///
	EXPERIENCE_UNSUPPORTED_PROPERTY    (ParserError.Severity.WARN,  "Unsupported property 'test/experience/%s' (Test [%s], Experience [%s])"),              ///
	ONVIEWS_NOT_LIST                   (ParserError.Severity.ERROR, "'tests/onViews' property must be a list (Test [%s])"),                                 ///
	ONVIEWS_LIST_EMPTY                 (ParserError.Severity.ERROR, "'tests/onViews' list must contain at least one element (Test [%s])"),                  ///
	ONVIEW_NOT_OBJECT                  (ParserError.Severity.ERROR, "'tests/onViews' list element must be an object (Test [%s])"),                          ///
	VIEWREF_NOT_STRING                 (ParserError.Severity.ERROR, "'tests/onViews/viewRef' property must be a string (Test [%s])"),                       ///
	VIEWREF_MISSING                    (ParserError.Severity.ERROR, "'tests/onViews/viewRef' property is missing (Test [%s])"),                             ///
	VIEWREF_UNDEFINED                  (ParserError.Severity.ERROR, "'tests/onViews/viewRef' property [%s] references an undefined view (Test [%s])"),      ///
	ISINVARIANT_NOT_BOOLEAN            (ParserError.Severity.ERROR, "'tests/onViews/isInvariant' property must be a boolean (Test [%s], ViewRef [%s])"),    ///
	VARIANTS_NOT_LIST                  (ParserError.Severity.ERROR, "'tests/onViews/variants' property must be a list (Test [%s], ViewRef [%s])"),          ///
	VARIANTS_LIST_EMPTY                (ParserError.Severity.ERROR, "'tests/onViews/variants' list must contain at least one element (Test [%s], ViewRef [%s])"), ///
	VARIANTS_UNSUPPORTED_PROPERTY      (ParserError.Severity.ERROR, "Unsupported property 'tests/onViews/variants/[%s]' (Test [%s], ViewRef [%s])"),        ///
	VARIANTS_ISINVARIANT_INCOMPATIBLE  (ParserError.Severity.ERROR, "Property 'tests/onViews' cannot be invariant and have variants (Test [%s], ViewRef [%s])"), ///
	VARIANTS_ISINVARIANT_XOR           (ParserError.Severity.ERROR, "Property 'tests/onViews' must specify one of: 'isInvariant' or 'variants' (Test [%s], ViewRef [%s])"), ///
	VARIANT_NOT_OBJECT                 (ParserError.Severity.ERROR, "'tests/onViews/variants' list element must be an object (Test [%s], ViewRef [%s])"),   ///
	EXPERIENCEREF_MISSING              (ParserError.Severity.ERROR, "'tests/onViews/variants/experienceRef' property is missing (Test [%s], ViewRef [%s])"), ///
	EXPERIENCEREF_NOT_STRING           (ParserError.Severity.ERROR, "'tests/onViews/variants/experienceRef' property must be a string (Test [%s], ViewRef [%s])"), ///
	EXPERIENCEREF_UNDEFINED            (ParserError.Severity.ERROR, "'tests/onViews/variants/experienceRef' property [%s] references an undefined expereince (Test [%s], ViewRef [%s])"), ///
	EXPERIENCEREF_ISCONTROL            (ParserError.Severity.ERROR, "'tests/onViews/variants/experienceRef' property [%s] cannot reference a control expereince (Test [%s], ViewRef [%s])"), ///
	EXPERIENCEREF_PATH_NOT_STRING      (ParserError.Severity.ERROR, "'tests/onViews/variants/path' property must be a string (Test [%s], ViewRef [%s], ExperienceRef [%s])"),

	// General errors
	UNSUPPORTED_CLAUSE                 (ParserError.Severity.WARN,  "Unsupported clause [%s]"),      ///
	INTERNAL                           (ParserError.Severity.FATAL, "Unexpectged error [%s]"),
	JSON_PARSE                         (ParserError.Severity.FATAL, "Invalid JSON syntax: [%s]");    /// 

	private ParserError.Severity severity;
	private String format;

	private ParserErrorTemplate(ParserError.Severity severity, String format) {
		this.severity = severity;
		this.format = format;
	}

	/**
	 *
	 * @return
	 */
	String getFormat() {
		return format;
	}
		
	/**
	 * 
	 * @return
	 */
	ParserError.Severity getSeverity() {
		return severity;
	}

}
