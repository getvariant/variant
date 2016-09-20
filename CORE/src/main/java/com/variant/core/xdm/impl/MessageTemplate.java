package com.variant.core.xdm.impl;

import com.variant.core.schema.ParserMessage.Severity;



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
	BOOT_EVENT_FLUSHER_NO_INTERFACE                       (Severity.FATAL, "Event flusher class [%s] must implement interface [%s]"), 
	BOOT_SID_TRACKER_NO_INTERFACE                         (Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]"),
	BOOT_SESSION_STORE_NO_INTERFACE                       (Severity.FATAL, "Session store class [%s] must implement interface [%s]"),
	BOOT_SESSION_ID_TRACKER_NO_INTERFACE                  (Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]"),
	BOOT_TARGETING_TRACKER_NO_INTERFACE                   (Severity.FATAL, "Targeting tracker class [%s] must implement interface [%s]"),
	BOOT_PARSER_LISTENER_NO_INTERFACE                     (Severity.FATAL, "Class [%s], annotated as [%s] must implement interface [%s]"),
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  PARSER MESSAGES                                                       //
	//------------------------------------------------------------------------------------------------------------------------//

	// State related errors
	PARSER_NO_STATES_CLAUSE                               (Severity.INFO,  "'/states' element is not found"),
	PARSER_NO_STATES                                      (Severity.INFO,  "No states found"), 
	PARSER_STATE_NAME_MISSING                             (Severity.ERROR, "State name is missing"), 
	PARSER_STATE_NAME_INVALID                             (Severity.ERROR, "State name must be a string, containing letters, digits and _, and cannot start with a digit"), 
	PARSER_STATE_NAME_DUPE                                (Severity.ERROR, "Duplicate state name [%s]"), 
	PARSER_STATE_PARAMS_NOT_OBJECT                        (Severity.ERROR, "'states/parameters' property must be an object (State [%s])"), 
	PARSER_STATE_UNSUPPORTED_PROPERTY                     (Severity.WARN,  "Unsupported property 'state/%s' (State [%s])"), 
	
	// Test related errors
	PARSER_NO_TESTS_CLAUSE                                (Severity.INFO,  "'/tests' element is missing"), 
	PARSER_NO_TESTS                                       (Severity.INFO,  "No tests found"), 
	PARSER_TEST_NAME_MISSING                              (Severity.ERROR, "Test name is missing"), 
	PARSER_TEST_NAME_INVALID                              (Severity.ERROR, "Test name must be a string, containing letters, digits and _, and cannot start with a digit"), 
	PARSER_TEST_NAME_DUPE                                 (Severity.ERROR, "Duplicate test name [%s]"), 
	PARSER_TEST_ISON_NOT_BOOLEAN                          (Severity.ERROR, "'tests/isOn' property must be a boolean (Test [%s])"), 
	PARSER_TEST_UNSUPPORTED_PROPERTY                      (Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"), 
	PARSER_EXPERIENCES_NOT_LIST                           (Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"), 
	PARSER_EXPERIENCES_LIST_EMPTY                         (Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"), 
	PARSER_EXPERIENCE_NOT_OBJECT                          (Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])"),  
	PARSER_EXPERIENCE_NAME_MISSING                        (Severity.ERROR, "Experience name is missing (Test [%s])"), 
	PARSER_EXPERIENCE_NAME_INVALID                        (Severity.ERROR, "Experience name must be a string containing letters, digits and _, and cannot start with a digit (Test [%s])"), 
	PARSER_EXPERIENCE_NAME_DUPE                           (Severity.ERROR, "Duplicate expereince name [%s] in test [%s]"), 
	PARSER_COVARIANT_TESTS_NOT_LIST                       (Severity.ERROR, "'tests/covariantTestRefs' property must be a list (Test [%s])"), 
	PARSER_COVARIANT_TESTREF_NOT_STRING                   (Severity.ERROR, "'tests/covariantTestRefs' list element must be a string (Test [%s])"), 
	PARSER_COVARIANT_TESTREF_UNDEFINED                    (Severity.ERROR, "Property 'tests/covariantTestRefs' references test [%s], which does not exist (Test [%s])"), 
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
	PARSER_STATEREF_UNDEFINED                             (Severity.ERROR, "'tests/onStates/stateRef' property [%s] references a state which does not exist (Test [%s])"), 
	PARSER_ALL_PROPER_EXPERIENCES_UNDEFINED               (Severity.ERROR, "At least one proper state variant must be defined (Test [%s], StateRef [%s])"), 
	PARSER_ISNONVARIANT_NOT_BOOLEAN                       (Severity.ERROR, "'tests/onStates/isNonvariant' property must be a boolean (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_NOT_LIST                              (Severity.ERROR, "'tests/onStates/variants' property must be a list (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_LIST_EMPTY                            (Severity.ERROR, "'tests/onStates/variants' list must contain at least one element (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_UNSUPPORTED_PROPERTY                  (Severity.ERROR, "Unsupported property 'tests/onStates/variants/[%s]' (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_ISNONVARIANT_INCOMPATIBLE             (Severity.ERROR, "Property 'tests/onStates' cannot be nonvariant and have variants (Test [%s], StateRef [%s])"), 
	PARSER_VARIANTS_ISNONVARIANT_XOR                      (Severity.ERROR, "Property 'tests/onStates' must specify one of: 'isNonvariant' or 'variants' (Test [%s], StateRef [%s])"), 
	PARSER_VARIANT_NOT_OBJECT                             (Severity.ERROR, "'tests/onStates/variants' list element must be an object (Test [%s], StateRef [%s])"), 
	PARSER_VARIANT_DUPE                                   (Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references experience [%s] (Test [%s], StateRef [%s])"), 
	PARSER_COVARIANT_VARIANT_DUPE                         (Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references covariant experience(s) [%s] (Test [%s], StateRef [%s], Experience [%s])"), 
	PARSER_VARIANT_MISSING                                (Severity.ERROR, "Variant element 'tests/onStates/variants' missing for experience [%s] (Test [%s], StateRef [%s])"), 
	PARSER_COVARIANT_VARIANT_MISSING                      (Severity.ERROR, "'tests/onStates/variants' list element missing for proper experience [%s] and covariant experience(s) [%s] (Test [%s], StateRef [%s])"), 	
	PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT           (Severity.ERROR, "Variant element 'tests/onStates/variants' for covariant experience [%s.%s] cannot refer to a non-covariant test (Test [%s], StateRef [%s])"), 	
	PARSER_COVARIANT_VARIANT_PROPER_UNDEFINED             (Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its proper experience is undefined (Test [%s], StateRef [%s])"), 	
	PARSER_COVARIANT_VARIANT_COVARIANT_UNDEFINED          (Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its covariant experience [%s] is undefined (Test [%s], StateRef [%s])"), 	
	PARSER_COVARIANT_EXPERIENCEREFS_NOT_ALLOWED           (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_EXPERIENCEREF_PARAMS_NOT_ALLOWED               (Severity.ERROR, "'tests/onStates/variants/parameters' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])"),
	PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST              (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property must be a list (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT            (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element must be an object (Test [%s], StateRef [%s], ExperienceRefs [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT   (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element cannot reference multiple tests, which are not covariant with each other (Test [%s], StateRef [%s], ExperienceRef [%s] CovariantExperienceRefs [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING       (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRefs' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED        (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' references test [%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT       (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' cannot reference test [%s] which is nonvariant on this state (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED  (Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRef' references a experience [%s.%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"), 
	PARSER_COVARIANT_EXPERIENCE_DUPE                      (Severity.ERROR, "Duplicate list element 'tests/onStates/variants/covariantExperienceRefs' references experience [%s.%s] (Test [%s], StateRef [%s], Experience [%s])"), 
	PARSER_ISDEFINED_NOT_BOOLEAN                          (Severity.ERROR, "'tests/onStates/variants/isDefined' property must be a boolean (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_MISSING                          (Severity.ERROR, "'tests/onStates/variants/experienceRef' property is missing (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_NOT_STRING                       (Severity.ERROR, "'tests/onStates/variants/experienceRef' property must be a string (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_UNDEFINED                        (Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] references an expereince that does not exist (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_ISCONTROL                        (Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] cannot reference a control expereince, unless undefined (Test [%s], StateRef [%s])"), 
	PARSER_EXPERIENCEREF_PARAMS_NOT_OBJECT                (Severity.ERROR, "'tests/onStates/variants/parameters' property must be an object (Test [%s], StateRef [%s], ExperienceRef [%s])"),

	// General parser errors
	PARSER_UNSUPPORTED_CLAUSE                             (Severity.WARN,  "Unsupported clause [%s]"), 
	PARSER_JSON_PARSE                                     (Severity.FATAL, "Invalid JSON syntax: [%s]"),  

	//------------------------------------------------------------------------------------------------------------------------//
	//                                                 RUN TIME MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//
	
	RUN_PROPERTY_BAD_CLASS                                (Severity.ERROR, "Don't know how to convert to class [%s]"),
	RUN_PROPERTY_INIT_INVALID_JSON                        (Severity.ERROR, "Invalid JSON [%s] in system property [%s]"),
	RUN_PROPERTY_INIT_PROPERTY_NOT_SET                    (Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]"),
	RUN_STATE_NOT_INSTRUMENTED_FOR_TEST                   (Severity.ERROR, "State [%s] is not instrumented for test [%s]"), 
	RUN_WEIGHT_MISSING                                    (Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found"),
	RUN_ACTIVE_REQUEST                                    (Severity.ERROR, "Commit current state request first"),
	RUN_SCHEMA_OBSOLETE                                   (Severity.ERROR, "Operation failed becuase this schema (ID [%s]) has been undeployed"),
	RUN_SCHEMA_MODIFIED                                   (Severity.ERROR, "Opereation failed because the current schema ID [%s] differs from that with which this session was created [%s]"),
	RUN_SCHEMA_UNDEFINED                                  (Severity.ERROR, "Cannot create a session on an idle Variant instance. Deploy a schema first"),
	RUN_SESSION_EXPIRED                                   (Severity.ERROR, "This session has expired"), 
	RUN_METHOD_UNSUPPORTED                                (Severity.ERROR, "Method unsupported in Core"),
	RUN_STATE_UNDEFINED_IN_EXPERIENCE                     (Severity.ERROR, "Currently active experience [%s] is undefined on state [%s]"),
	RUN_HOOK_TARGETING_BAD_EXPERIENCE                     (Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s]"),
	RUN_HOOK_TARGETING_UNDEFINED_EXPERIENCE               (Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s], which is undefined on state [%s]"),
 
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  GENERAL MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//

	HOOK_LISTENER_EXCEPTION                               (Severity.ERROR, "User hook listener class [%s] threw an exception [%s]. See logs for details."),
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
