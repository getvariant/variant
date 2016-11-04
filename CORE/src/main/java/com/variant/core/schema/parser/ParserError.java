package com.variant.core.schema.parser;

import com.variant.core.exception.Error;

public class ParserError extends Error {

	// 
	// State related errors
	//
	public static final ParserError NO_STATES_CLAUSE =
			new ParserError(Severity.INFO,  "'/states' element is not found");
	
	public static final ParserError NO_STATES = 
			new ParserError(Severity.INFO, "No states found"); 
	
	public static final ParserError STATE_NAME_MISSING =
			new ParserError(Severity.ERROR, "State name is missing"); 
	
	public static final ParserError STATE_NAME_INVALID =
			new ParserError(Severity.ERROR, "State name must be a string, containing letters, digits and _, and cannot start with a digit"); 
	
	public static final ParserError STATE_NAME_DUPE =
			new ParserError(Severity.ERROR, "Duplicate state name [%s]"); 
	
	public static final ParserError STATE_PARAMS_NOT_OBJECT =
			new ParserError(Severity.ERROR, "'states/parameters' property must be an object (State [%s])"); 
	
	public static final ParserError STATE_UNSUPPORTED_PROPERTY =
			new ParserError(Severity.WARN,  "Unsupported property 'state/%s' (State [%s])"); 
	
	//
	// Test related errors
	//
	public static final ParserError NO_TESTS_CLAUSE =
			new ParserError(Severity.INFO,  "'/tests' element is missing"); 
	
	public static final ParserError NO_TESTS =
			new ParserError(Severity.INFO,  "No tests found"); 
	
	public static final ParserError TEST_NAME_MISSING =
			new ParserError(Severity.ERROR, "Test name is missing"); 
	
	public static final ParserError TEST_NAME_INVALID =
			new ParserError(Severity.ERROR, "Test name must be a string, containing letters, digits and _, and cannot start with a digit"); 
	
	public static final ParserError TEST_NAME_DUPE =
			new ParserError(Severity.ERROR, "Duplicate test name [%s]"); 
	
	public static final ParserError TEST_ISON_NOT_BOOLEAN =
			new ParserError(Severity.ERROR, "'tests/isOn' property must be a boolean (Test [%s])"); 
	
	public static final ParserError TEST_UNSUPPORTED_PROPERTY =
			new ParserError(Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"); 
	
	public static final ParserError EXPERIENCES_NOT_LIST =
			new ParserError(Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"); 
	
	public static final ParserError EXPERIENCES_LIST_EMPTY =
			new ParserError(Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"); 
	
	public static final ParserError EXPERIENCE_NOT_OBJECT =
			new ParserError(Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])");  
	
	public static final ParserError EXPERIENCE_NAME_MISSING =
			new ParserError(Severity.ERROR, "Experience name is missing (Test [%s])"); 
	
	public static final ParserError EXPERIENCE_NAME_INVALID =
			new ParserError(Severity.ERROR, "Experience name must be a string containing letters, digits and _, and cannot start with a digit (Test [%s])"); 
	
	public static final ParserError EXPERIENCE_NAME_DUPE =
			new ParserError(Severity.ERROR, "Duplicate expereince name [%s] in test [%s]"); 
	
	public static final ParserError COVARIANT_TESTS_NOT_LIST =
			new ParserError(Severity.ERROR, "'tests/covariantTestRefs' property must be a list (Test [%s])"); 
	
	public static final ParserError COVARIANT_TESTREF_NOT_STRING =
			new ParserError(Severity.ERROR, "'tests/covariantTestRefs' list element must be a string (Test [%s])"); 
	
	public static final ParserError COVARIANT_TESTREF_UNDEFINED =
			new ParserError(Severity.ERROR, "Property 'tests/covariantTestRefs' references test [%s], which does not exist (Test [%s])"); 
	
	public static final ParserError COVARIANT_TEST_DISJOINT =
			new ParserError(Severity.ERROR, "Covariant test [%s] cannot be disjoint (Test [%s])"); 
	
	public static final ParserError ISCONTROL_NOT_BOOLEAN =
			new ParserError(Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"); 
	
	public static final ParserError CONTROL_EXPERIENCE_DUPE =
			new ParserError(Severity.ERROR, "Duplicate control experience [%s] in test [%s]"); 
	
	public static final ParserError CONTROL_EXPERIENCE_MISSING =
			new ParserError(Severity.ERROR, "Control experience is missing in test [%s]"); 
	
	public static final ParserError WEIGHT_NOT_NUMBER =
			new ParserError(Severity.ERROR, "'tests/experience/weight' property must be a number (Test [%s], Experience [%s])"); 
	
	public static final ParserError EXPERIENCE_UNSUPPORTED_PROPERTY =
			new ParserError(Severity.WARN,  "Unsupported property 'test/experience/%s' (Test [%s], Experience [%s])"); 
	
	public static final ParserError ONSTATES_NOT_LIST =
			new ParserError(Severity.ERROR, "'tests/onStates' property must be a list (Test [%s])"); 
	
	public static final ParserError ONSTATES_LIST_EMPTY =
			new ParserError(Severity.ERROR, "'tests/onStates' list must contain at least one element (Test [%s])"); 
	
	public static final ParserError ONSTATES_NOT_OBJECT =
			new ParserError(Severity.ERROR, "'tests/onStates' list element must be an object (Test [%s])"); 
	
	public static final ParserError STATEREF_NOT_STRING =
			new ParserError(Severity.ERROR, "'tests/onStates/stateRef' property must be a string (Test [%s])"); 
	
	public static final ParserError STATEREF_MISSING =
			new ParserError(Severity.ERROR, "'tests/onStates/stateRef' property is missing (Test [%s])"); 
	
	public static final ParserError STATEREF_DUPE =
			new ParserError(Severity.ERROR, "Duplicate property 'tests/onStates/stateRef' [%s] (Test [%s])"); 
	
	public static final ParserError STATEREF_UNDEFINED =
			new ParserError(Severity.ERROR, "'tests/onStates/stateRef' property [%s] references a state which does not exist (Test [%s])"); 
	
	public static final ParserError ALL_PROPER_EXPERIENCES_UNDEFINED =
			new ParserError(Severity.ERROR, "At least one proper state variant must be defined (Test [%s], StateRef [%s])"); 
	
	public static final ParserError ISNONVARIANT_NOT_BOOLEAN =
			new ParserError(Severity.ERROR, "'tests/onStates/isNonvariant' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_NOT_LIST =
			new ParserError(Severity.ERROR, "'tests/onStates/variants' property must be a list (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_LIST_EMPTY =
			new ParserError(Severity.ERROR, "'tests/onStates/variants' list must contain at least one element (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_UNSUPPORTED_PROPERTY =
			new ParserError(Severity.ERROR, "Unsupported property 'tests/onStates/variants/[%s]' (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_ISNONVARIANT_INCOMPATIBLE =
			new ParserError(Severity.ERROR, "Property 'tests/onStates' cannot be nonvariant and have variants (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_ISNONVARIANT_XOR =
			new ParserError(Severity.ERROR, "Property 'tests/onStates' must specify one of: 'isNonvariant' or 'variants' (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANT_NOT_OBJECT =
			new ParserError(Severity.ERROR, "'tests/onStates/variants' list element must be an object (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANT_DUPE =
			new ParserError(Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references experience [%s] (Test [%s], StateRef [%s])"); 
	
	public static final ParserError COVARIANT_VARIANT_DUPE =
			new ParserError(Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references covariant experience(s) [%s] (Test [%s], StateRef [%s], Experience [%s])"); 
	
	public static final ParserError VARIANT_MISSING =
			new ParserError(Severity.ERROR, "Variant element 'tests/onStates/variants' missing for experience [%s] (Test [%s], StateRef [%s])"); 
	
	public static final ParserError COVARIANT_VARIANT_MISSING =
			new ParserError(Severity.ERROR, "'tests/onStates/variants' list element missing for proper experience [%s] and covariant experience(s) [%s] (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_VARIANT_TEST_NOT_COVARIANT =
			new ParserError(Severity.ERROR, "Variant element 'tests/onStates/variants' for covariant experience [%s.%s] cannot refer to a non-covariant test (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_VARIANT_PROPER_UNDEFINED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its proper experience is undefined (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_VARIANT_COVARIANT_UNDEFINED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its covariant experience [%s] is undefined (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_EXPERIENCEREFS_NOT_ALLOWED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_PARAMS_NOT_ALLOWED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/parameters' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])");
	
	public static final ParserError COVARIANT_EXPERIENCEREFS_NOT_LIST =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property must be a list (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_REF_NOT_OBJECT =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element must be an object (Test [%s], StateRef [%s], ExperienceRefs [%s])"); 

	public static final ParserError COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element cannot reference multiple tests, which are not covariant with each other (Test [%s], StateRef [%s], ExperienceRef [%s] CovariantExperienceRefs [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRefs' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' references test [%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' cannot reference test [%s] which is nonvariant on this state (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRef' references a experience [%s.%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_DUPE =
			new ParserError(Severity.ERROR, "Duplicate list element 'tests/onStates/variants/covariantExperienceRefs' references experience [%s.%s] (Test [%s], StateRef [%s], Experience [%s])"); 
	
	public static final ParserError ISDEFINED_NOT_BOOLEAN =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/isDefined' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_MISSING =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/experienceRef' property is missing (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_NOT_STRING =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/experienceRef' property must be a string (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_UNDEFINED =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] references an expereince that does not exist (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_ISCONTROL =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] cannot reference a control expereince, unless undefined (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_PARAMS_NOT_OBJECT =
			new ParserError(Severity.ERROR, "'tests/onStates/variants/parameters' property must be an object (Test [%s], StateRef [%s], ExperienceRef [%s])");

	// General parser errors
	public static final ParserError UNSUPPORTED_CLAUSE =
			new ParserError(Severity.WARN,  "Unsupported clause [%s]");
	
	public static final ParserError JSON_PARSE =
			new ParserError(Severity.FATAL, "Invalid JSON syntax: [%s]");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected ParserError(Severity severity, String format) {
		super(severity, format);
	}

}
