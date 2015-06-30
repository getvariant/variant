package com.variant.core.error;


/**
 * The immutable, uncontextualized part of the error.
 * 
 * @author Igor
 */
public enum ErrorTemplate {
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                   PARSER ERRORS                                                        //
	//------------------------------------------------------------------------------------------------------------------------//

	// View related errors
	PARSER_NO_VIEWS_CLAUSE                    (Severity.INFO, "VIEWS clause is not found"),                                                            /// junit exists.
	PARSER_NO_VIEWS                           (Severity.INFO, "No views found"),                                                                       ///
	PARSER_VIEW_NAME_MISSING                  (Severity.ERROR, "View name is missing"),                                                                ///
	PARSER_VIEW_NAME_NOT_STRING               (Severity.ERROR, "View name must be a string"),                                                          ///
	PARSER_VIEW_NAME_DUPE                     (Severity.ERROR, "Duplicate view name [%s]"),                                                            ///
	PARSER_VIEW_PATH_MISSING                  (Severity.ERROR, "'view/path' property is missing (View [%s])"),                                         ///
	PARSER_VIEW_PATH_NOT_STRING               (Severity.ERROR, "'view/path' property must be a string (View [%s])"),                                   ///
	PARSER_VIEW_UNSUPPORTED_PROPERTY          (Severity.WARN,  "Unsupported property 'view/%s' (View [%s])"),                                          ///
	
	// Test related errors
	PARSER_NO_TESTS_CLAUSE                    (Severity.INFO, "'tests' clause is missing"),                                                            ///
	PARSER_NO_TESTS                           (Severity.INFO, "No tests found"),                                                                       ///
	PARSER_TEST_NAME_MISSING                  (Severity.ERROR, "Test name is missing"),                                                                ///
	PARSER_TEST_NAME_NOT_STRING               (Severity.ERROR, "Test name must be a string"),                                                          ///
	PARSER_TEST_NAME_DUPE                     (Severity.ERROR, "Duplicate test name [%s]"),                                                            ///
	PARSER_TEST_UNSUPPORTED_PROPERTY          (Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"),                                         ///
	PARSER_EXPERIENCES_NOT_LIST               (Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"),                             ///
	PARSER_EXPERIENCES_LIST_EMPTY             (Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"),              ///
	PARSER_EXPERIENCE_NOT_OBJECT              (Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])"),                      /// 
	PARSER_EXPERIENCE_NAME_NOT_STRING         (Severity.ERROR, "'tests/experience/name' property must be a string (Test [%s])"),                       ///
	PARSER_ISCONTROL_NOT_BOOLEAN              (Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"),///
	PARSER_WEIGHT_NOT_NUMBER                  (Severity.ERROR, "'tests/experience/weight' property must be a number (Test [%s], Experience [%s])"),    ///
	PARSER_EXPERIENCE_UNSUPPORTED_PROPERTY    (Severity.WARN,  "Unsupported property 'test/experience/%s' (Test [%s], Experience [%s])"),              ///
	PARSER_ONVIEWS_NOT_LIST                   (Severity.ERROR, "'tests/onViews' property must be a list (Test [%s])"),                                 ///
	PARSER_ONVIEWS_LIST_EMPTY                 (Severity.ERROR, "'tests/onViews' list must contain at least one element (Test [%s])"),                  ///
	PARSER_ONVIEW_NOT_OBJECT                  (Severity.ERROR, "'tests/onViews' list element must be an object (Test [%s])"),                          ///
	PARSER_VIEWREF_NOT_STRING                 (Severity.ERROR, "'tests/onViews/viewRef' property must be a string (Test [%s])"),                       ///
	PARSER_VIEWREF_MISSING                    (Severity.ERROR, "'tests/onViews/viewRef' property is missing (Test [%s])"),                             ///
	PARSER_VIEWREF_UNDEFINED                  (Severity.ERROR, "'tests/onViews/viewRef' property [%s] references an undefined view (Test [%s])"),      ///
	PARSER_ISINVARIANT_NOT_BOOLEAN            (Severity.ERROR, "'tests/onViews/isInvariant' property must be a boolean (Test [%s], ViewRef [%s])"),    ///
	PARSER_VARIANTS_NOT_LIST                  (Severity.ERROR, "'tests/onViews/variants' property must be a list (Test [%s], ViewRef [%s])"),          ///
	PARSER_VARIANTS_LIST_EMPTY                (Severity.ERROR, "'tests/onViews/variants' list must contain at least one element (Test [%s], ViewRef [%s])"), ///
	PARSER_VARIANTS_UNSUPPORTED_PROPERTY      (Severity.ERROR, "Unsupported property 'tests/onViews/variants/[%s]' (Test [%s], ViewRef [%s])"),        ///
	PARSER_VARIANTS_ISINVARIANT_INCOMPATIBLE  (Severity.ERROR, "Property 'tests/onViews' cannot be invariant and have variants (Test [%s], ViewRef [%s])"), ///
	PARSER_VARIANTS_ISINVARIANT_XOR           (Severity.ERROR, "Property 'tests/onViews' must specify one of: 'isInvariant' or 'variants' (Test [%s], ViewRef [%s])"), ///
	PARSER_VARIANT_NOT_OBJECT                 (Severity.ERROR, "'tests/onViews/variants' list element must be an object (Test [%s], ViewRef [%s])"),   ///
	PARSER_EXPERIENCEREF_MISSING              (Severity.ERROR, "'tests/onViews/variants/experienceRef' property is missing (Test [%s], ViewRef [%s])"), ///
	PARSER_EXPERIENCEREF_NOT_STRING           (Severity.ERROR, "'tests/onViews/variants/experienceRef' property must be a string (Test [%s], ViewRef [%s])"), ///
	PARSER_EXPERIENCEREF_UNDEFINED            (Severity.ERROR, "'tests/onViews/variants/experienceRef' property [%s] references an undefined expereince (Test [%s], ViewRef [%s])"), ///
	PARSER_EXPERIENCEREF_ISCONTROL            (Severity.ERROR, "'tests/onViews/variants/experienceRef' property [%s] cannot reference a control expereince (Test [%s], ViewRef [%s])"), ///
	PARSER_EXPERIENCEREF_PATH_NOT_STRING      (Severity.ERROR, "'tests/onViews/variants/path' property must be a string (Test [%s], ViewRef [%s], ExperienceRef [%s])"),

	// General errors
	PARSER_UNSUPPORTED_CLAUSE                 (Severity.WARN,  "Unsupported clause [%s]"),                                                ///
	PARSER_JSON_PARSE                         (Severity.FATAL, "Invalid JSON syntax: [%s]"),                                              /// 

	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  RUN TIME ERRORS                                                       //
	//------------------------------------------------------------------------------------------------------------------------//
	
	RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST        (Severity.ERROR, "View [%s] is not instrumented for test [%s]"),                            ///
	RUN_NO_VIEW_FOR_PATH                      (Severity.ERROR, "No view matches path [%s]"),
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                   GENERAL ERRORS                                                       //
	//------------------------------------------------------------------------------------------------------------------------//

	INTERNAL                           (Severity.FATAL, "Unexpectged error [%s]");

	//------------------------------------------------------------------------------------------------------------------------//

	private Severity severity;
	private String format;

	private ErrorTemplate(Severity severity, String format) {
		this.severity = severity;
		this.format = format;
	}

	/**
	 *
	 * @return
	 */
	public String getFormat() {
		return format;
	}
		
	/**
	 * 
	 * @return
	 */
	public Severity getSeverity() {
		return severity;
	}

}
