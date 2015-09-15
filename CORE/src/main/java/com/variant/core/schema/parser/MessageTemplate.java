package com.variant.core.schema.parser;



/**
 * The immutable, uncontextualized part of a system message.
 * 
 * @author Igor
 */
public enum MessageTemplate {
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                BOOTSTRAP MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//
 
	BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN              (Severity.FATAL, "Cannot pass both -Dvariant.config.resource and -Dvariant.config.file parameters"), 
	BOOT_CONFIG_RESOURCE_NOT_FOUND                        (Severity.FATAL, "Class path resource [%s] is not found"), 
	BOOT_CONFIG_FILE_NOT_FOUND                            (Severity.FATAL, "OS file [%s] is not found"), 
	BOOT_EVENT_PERSISTER_NO_INTERFACE                     (Severity.FATAL, "Event persister class [%s] must implement interface [%s]"), 
	BOOT_SID_PERSISTER_NO_INTERFACE                       (Severity.FATAL, "Session ID persister class [%s] must implement interface [%s]"),
	BOOT_TARGETING_PERSISTER_NO_INTERFACE                 (Severity.FATAL, "Targeting persister class [%s] must implement interface [%s]"),
	BOOT_PARSER_LISTENER_NO_INTERFACE                     (Severity.FATAL, "Class [%s], annotated as [%s] must implement interface [%s]"),
	BOOT_PARSER_LISTENER_EXCEPTION                        (Severity.FATAL, "Perser event listener [%s] threw an exception [%s] for target [%s]"),
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  PARSER MESSAGES                                                       //
	//------------------------------------------------------------------------------------------------------------------------//

	// State related errors
	PARSER_NO_STATES_CLAUSE                               (Severity.INFO,  "'/states' element is not found"),
	PARSER_NO_STATES                                      (Severity.INFO,  "No states found"), 
	PARSER_STATE_NAME_MISSING                             (Severity.ERROR, "State name is missing"), 
	PARSER_STATE_NAME_NOT_STRING                          (Severity.ERROR, "State name must be a string"), 
	PARSER_STATE_NAME_DUPE                                (Severity.ERROR, "Duplicate state name [%s]"), 
	PARSER_STATE_PARAMS_MISSING                           (Severity.ERROR, "'states/parameters' property is missing (State [%s])"), 
	PARSER_STATE_PARAMS_EMPTY                             (Severity.ERROR, "'states/parameters' property must contain at least one element (State [%s])"), 
	PARSER_STATE_PARAMS_NOT_OBJECT                        (Severity.ERROR, "'states/parameters' property must be an object (State [%s])"), 
	PARSER_STATE_UNSUPPORTED_PROPERTY                     (Severity.WARN,  "Unsupported property 'state/%s' (State [%s])"), 
	
	// Test related errors
	PARSER_NO_TESTS_CLAUSE                                (Severity.INFO,  "'/tests' element is missing"), 
	PARSER_NO_TESTS                                       (Severity.INFO,  "No tests found"), 
	PARSER_TEST_NAME_MISSING                              (Severity.ERROR, "Test name is missing"), 
	PARSER_TEST_NAME_NOT_STRING                           (Severity.ERROR, "Test name must be a string"), 
	PARSER_TEST_NAME_DUPE                                 (Severity.ERROR, "Duplicate test name [%s]"), 
	PARSER_TEST_ISON_NOT_BOOLEAN                          (Severity.ERROR, "'tests/isOn' property must be a boolean (Test [%s])"), 
	PARSER_TEST_IDLE_DAYS_TO_LIVE_NOT_INT                 (Severity.ERROR, "'tests/idleDaysToLive' property must be an integer (Test [%s])"), 
	PARSER_TEST_IDLE_DAYS_TO_LIVE_NEGATIVE                (Severity.ERROR, "'tests/idleDaysToLive' property cannot be negative (Test [%s])"), 
	PARSER_TEST_UNSUPPORTED_PROPERTY                      (Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"), 
	PARSER_EXPERIENCES_NOT_LIST                           (Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"), 
	PARSER_EXPERIENCES_LIST_EMPTY                         (Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"), 
	PARSER_EXPERIENCE_NOT_OBJECT                          (Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])"),  
	PARSER_EXPERIENCE_NAME_NOT_STRING                     (Severity.ERROR, "'tests/experience/name' property must be a string (Test [%s])"), 
	PARSER_EXPERIENCE_NAME_DUPE                           (Severity.ERROR, "Duplicate expereince name [%s] in test [%s]"), 
	PARSER_COVARIANT_TESTS_NOT_LIST                       (Severity.ERROR, "'tests/covariantTestRefs' property must be a list (Test [%s])"), 
	PARSER_COVARIANT_TESTREF_NOT_STRING                   (Severity.ERROR, "'tests/covariantTestRefs' list element must be a string (Test [%s])"), 
	PARSER_COVARIANT_TESTREF_UNDEFINED                    (Severity.ERROR, "Property 'tests/covariantTestRefs' references an undefined test [%s] (Test [%s])"), 
	PARSER_COVARIANT_TEST_DISJOINT                        (Severity.ERROR, "Covariant test [%s] cannot be disjoint (Test [%s])"), 
	PARSER_ISCONTROL_NOT_BOOLEAN                          (Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"), 
	PARSER_CONTROL_EXPERIENCE_DUPE                        (Severity.ERROR, "Duplicate control experience [%s] in test [%s]"), 
	PARSER_IS_CONTROL_MISSING                             (Severity.ERROR, "Control experience is missing in test [%s]"), 
	PARSER_WEIGHT_NOT_NUMBER                              (Severity.ERROR, "'tests/experience/weight' property must be a number (Test [%s], Experience [%s])"), 
	PARSER_EXPERIENCE_UNSUPPORTED_PROPERTY                (Severity.WARN,  "Unsupported property 'test/experience/%s' (Test [%s], Experience [%s])"), 
	PARSER_ONSTATES_NOT_LIST                              (Severity.ERROR, "'tests/onStates' property must be a list (Test [%s])"), 
	PARSER_ONSTATES_LIST_EMPTY                            (Severity.ERROR, "'tests/onStates' list must contain at least one element (Test [%s])"), 
	PARSER_ONSTATES_NOT_OBJECT                            (Severity.ERROR, "'tests/onStates' list element must be an object (Test [%s])"), 
	PARSER_STATEREF_NOT_STRING                            (Severity.ERROR, "'tests/onStates/stateRef' property must be a string (Test [%s])"), 
	PARSER_STATEREF_MISSING                               (Severity.ERROR, "'tests/onStates/stateRef' property is missing (Test [%s])"), 
	PARSER_STATEREF_DUPE                                  (Severity.ERROR, "Duplicate property 'tests/onStates/stateRef' [%s] (Test [%s])"), 
	PARSER_STATEREF_UNDEFINED                             (Severity.ERROR, "'tests/onStates/stateRef' property [%s] references an undefined state (Test [%s])"), 
	PARSER_ISNONVARIANT_NOT_BOOLEAN                       (Severity.ERROR, "'tests/onStates/isNonvariant' property must be a boolean (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_NOT_LIST                              (Severity.ERROR, "'tests/onStates/variants' property must be a list (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_LIST_EMPTY                            (Severity.ERROR, "'tests/onStates/variants' list must contain at least one element (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_UNSUPPORTED_PROPERTY                  (Severity.ERROR, "Unsupported property 'tests/onStates/variants/[%s]' (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_ISNONVARIANT_INCOMPATIBLE             (Severity.ERROR, "Property 'tests/onStates' cannot be nonvariant and have variants (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_ISNONVARIANT_XOR                      (Severity.ERROR, "Property 'tests/onStates' must specify one of: 'isNonvariant' or 'variants' (Test [%s], StateRef [%s])"), 
	PARSER_VARIANT_NOT_OBJECT                             (Severity.ERROR, "'tests/onStates/variants' list element must be an object (Test [%s], StateRef [%s])"), 
	PARSER_VARIANT_DUPE                                   (Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references experience [%s] (Test [%s], StateRef [%s])"), 
	PARSER_COVARIANT_VARIANT_DUPE                         (Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references covariant experiences [%s] (Test [%s], StateRef [%s], Experience [%s])"), 
	PARSER_VARIANT_MISSING                                (Severity.ERROR, "Variant element 'tests/onStates/variants' missing for experience [%s] (Test [%s], StateRef [%s])"), 
	PARSER_COVARIANT_VARIANT_MISSING                      (Severity.ERROR, "Variant element 'tests/onStates/variants' missing for covariant experiences [%s] (Test [%s], StateRef [%s], Experience [%s])"), 	
	PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT           (Severity.ERROR, "Variant element 'tests/onStates/variants' for covariant experience [%s.%s] cannot refer to a non-covariant test (Test [%s], StateRef [%s])"), 	
	PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST              (Severity.ERROR, "'tests/onStates/covariantExperienceRefs' property must be a list (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT            (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element must be an object (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING       (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRefs' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED        (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' references an undefined test [%s] (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT       (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' cannot reference test [%s] which is nonvariant on this state (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED  (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRef' references an undefined experience [%s.%s] (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_DUPE                      (Severity.ERROR, "Duplicate list element 'tests/onStates/variants/covariantExperienceRefs' references experience [%s.%s] (Test [%s], StateRef [%s], Experience [%s])"), 
	PARSER_EXPERIENCEREF_MISSING                          (Severity.ERROR, "'tests/onStates/variants/experienceRef' property is missing (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_NOT_STRING                       (Severity.ERROR, "'tests/onStates/variants/experienceRef' property must be a string (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_UNDEFINED                        (Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] references an undefined expereince (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_ISCONTROL                        (Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] cannot reference a control expereince (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_PARAMS_NOT_OBJECT                (Severity.ERROR, "'tests/onStates/variants/parameters' property must be an object (Test [%s], StateRef [%s], ExperienceRef [%s])"),

	// General parser errors
	PARSER_UNSUPPORTED_CLAUSE                             (Severity.WARN,  "Unsupported clause [%s]"), 
	PARSER_JSON_PARSE                                     (Severity.FATAL, "Invalid JSON syntax: [%s]"),  

	//------------------------------------------------------------------------------------------------------------------------//
	//                                                 RUN TIME MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//
	
	RUN_STATE_NOT_INSTRUMENTED_FOR_TEST                   (Severity.ERROR, "State [%s] is not instrumented for test [%s]"), 
	RUN_PROPERTY_NOT_SET                                  (Severity.ERROR, "Property [%s] must be set"),
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  GENERAL MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//

	INTERNAL                                              (Severity.FATAL, "Unexpectged error [%s]. See log for details.");

	//------------------------------------------------------------------------------------------------------------------------//

	private Severity severity;
	private String format;

	private MessageTemplate(Severity severity, String format) {
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
