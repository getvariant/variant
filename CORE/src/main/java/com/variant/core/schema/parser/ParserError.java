package com.variant.core.schema.parser;

import com.variant.core.UserError;

public class ParserError extends UserError {

	// 
	// 001-020 Schema parser, Meta 
	//
	public static final ParserError NO_META_CLAUSE =
			new ParserError(1, Severity.ERROR,  "'/meta' clause is missing");

	public static final ParserError META_NOT_OBJECT =
			new ParserError(2, Severity.ERROR, "'/meta' property must be an object"); 

	public static final ParserError META_NAME_INVALID =
			new ParserError(3, Severity.ERROR, "Schema name must be a string, containing letters, digits and _, and cannot start with a digit"); 

	public static final ParserError META_NAME_MISSING =
			new ParserError(4, Severity.ERROR, "Schema name is missing"); 

	public static final ParserError META_COMMENT_INVALID =
			new ParserError(5, Severity.ERROR, "Schema comment must be a string"); 

	public static final ParserError META_UNSUPPORTED_PROPERTY =
			new ParserError(6, Severity.WARN,  "Unsupported property 'meta/%s'"); 

	public static final ParserError META_HOOK_NAME_INVALID =
			new ParserError(7, Severity.ERROR, "Hook name must be a string, containing letters, digits and _, and cannot start with a digit"); 

	public static final ParserError META_CLASS_NAME_INVALID =
			new ParserError(8, Severity.ERROR, "Class name must be a string"); 
	
	public static final ParserError META_HOOK_UNSUPPORTED_PROPERTY =
			new ParserError(9, Severity.WARN,  "Unsupported hook property [%s]"); 

	public static final ParserError META_HOOK_NAME_MISSING =
			new ParserError(10, Severity.ERROR, "Hook name missing"); 

	public static final ParserError META_HOOK_CLASS_NAME_MISSING =
			new ParserError(11, Severity.ERROR, "Hook class name missing for hook [%s]"); 

	public static final ParserError META_HOOK_NAME_DUPE =
			new ParserError(12, Severity.ERROR, "Duplicate hook name [%s]"); 

	public static final ParserError META_HOOKS_NOT_LIST = 
			new ParserError(13, Severity.ERROR, "'/meta/hooks' property must be a list"); 

	public static final ParserError META_HOOKS_NOT_OBJECT =
			new ParserError(14, Severity.ERROR, "'meta/hooks' property element must be an object"); 

	// 
	// 021-050 Schema parser, State
	//
	public static final ParserError NO_STATES_CLAUSE =
			new ParserError(21, Severity.INFO,  "'/states' clause is missing");
	
	public static final ParserError STATES_CLAUSE_NOT_LIST = 
			new ParserError(22, Severity.ERROR, "'/states' must be a list"); 
	
	public static final ParserError NO_STATES = 
			new ParserError(23, Severity.INFO, "No states found"); 

	public static final ParserError STATE_NAME_MISSING =
			new ParserError(24, Severity.ERROR, "State name is missing"); 
	
	public static final ParserError STATE_NAME_INVALID =
			new ParserError(25, Severity.ERROR, "State name must be a string, containing letters, digits and _, and cannot start with a digit"); 
	
	public static final ParserError STATE_NAME_DUPE =
			new ParserError(26, Severity.ERROR, "Duplicate state name [%s]"); 
	
	public static final ParserError STATE_PARAMS_NOT_OBJECT =
			new ParserError(27, Severity.ERROR, "'states/parameters' property element must be an object (State [%s])"); 
	
	public static final ParserError STATE_UNSUPPORTED_PROPERTY =
			new ParserError(28, Severity.WARN,  "Unsupported property 'state/%s' (State [%s])"); 
	
	// 
	// 051-150 Schema parser, Test
	//
	public static final ParserError NO_TESTS_CLAUSE =
			new ParserError(51, Severity.INFO,  "'/tests' clause is missing"); 
	
	public static final ParserError NO_TESTS =
			new ParserError(52, Severity.INFO,  "No tests found"); 
	
	public static final ParserError TEST_NAME_MISSING =
			new ParserError(53, Severity.ERROR, "Test name is missing"); 
	
	public static final ParserError TEST_NAME_INVALID =
			new ParserError(54, Severity.ERROR, "Test name must be a string, containing letters, digits and _, and cannot start with a digit"); 
	
	public static final ParserError TEST_NAME_DUPE =
			new ParserError(55, Severity.ERROR, "Duplicate test name [%s]"); 
	
	public static final ParserError TEST_ISON_NOT_BOOLEAN =
			new ParserError(56, Severity.ERROR, "'tests/isOn' property must be a boolean (Test [%s])"); 
	
	public static final ParserError TEST_UNSUPPORTED_PROPERTY =
			new ParserError(57, Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"); 
	
	public static final ParserError EXPERIENCES_NOT_LIST =
			new ParserError(58, Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"); 
	
	public static final ParserError EXPERIENCES_LIST_EMPTY =
			new ParserError(59, Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"); 
	
	public static final ParserError EXPERIENCE_NOT_OBJECT =
			new ParserError(60, Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])");  
	
	public static final ParserError EXPERIENCE_NAME_MISSING =
			new ParserError(61, Severity.ERROR, "Experience name is missing (Test [%s])"); 
	
	public static final ParserError EXPERIENCE_NAME_INVALID =
			new ParserError(62, Severity.ERROR, "Experience name must be a string containing letters, digits and _, and cannot start with a digit (Test [%s])"); 
	
	public static final ParserError EXPERIENCE_NAME_DUPE =
			new ParserError(63, Severity.ERROR, "Duplicate expereince name [%s] in test [%s]"); 
	
	public static final ParserError COVARIANT_TESTS_NOT_LIST =
			new ParserError(64, Severity.ERROR, "'tests/covariantTestRefs' property must be a list (Test [%s])"); 
	
	public static final ParserError COVARIANT_TESTREF_NOT_STRING =
			new ParserError(65, Severity.ERROR, "'tests/covariantTestRefs' list element must be a string (Test [%s])"); 
	
	public static final ParserError COVARIANT_TESTREF_UNDEFINED =
			new ParserError(66, Severity.ERROR, "Property 'tests/covariantTestRefs' references test [%s], which does not exist (Test [%s])"); 
	
	public static final ParserError COVARIANT_TEST_DISJOINT =
			new ParserError(67, Severity.ERROR, "Covariant test [%s] cannot be disjoint (Test [%s])"); 
	
	public static final ParserError ISCONTROL_NOT_BOOLEAN =
			new ParserError(68, Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"); 
	
	public static final ParserError CONTROL_EXPERIENCE_DUPE =
			new ParserError(69, Severity.ERROR, "Duplicate control experience [%s] in test [%s]"); 
	
	public static final ParserError CONTROL_EXPERIENCE_MISSING =
			new ParserError(70, Severity.ERROR, "Control experience is missing in test [%s]"); 
	
	public static final ParserError WEIGHT_NOT_NUMBER =
			new ParserError(71, Severity.ERROR, "'tests/experience/weight' property must be a number (Test [%s], Experience [%s])"); 
	
	public static final ParserError EXPERIENCE_UNSUPPORTED_PROPERTY =
			new ParserError(72, Severity.WARN,  "Unsupported property 'test/experience/%s' (Test [%s], Experience [%s])"); 
	
	public static final ParserError ONSTATES_NOT_LIST =
			new ParserError(73, Severity.ERROR, "'tests/onStates' property must be a list (Test [%s])"); 
	
	public static final ParserError ONSTATES_LIST_EMPTY =
			new ParserError(74, Severity.ERROR, "'tests/onStates' list must contain at least one element (Test [%s])"); 
	
	public static final ParserError ONSTATES_NOT_OBJECT =
			new ParserError(75, Severity.ERROR, "'tests/onStates' list element must be an object (Test [%s])"); 
	
	public static final ParserError STATEREF_NOT_STRING =
			new ParserError(76, Severity.ERROR, "'tests/onStates/stateRef' property must be a string (Test [%s])"); 
	
	public static final ParserError STATEREF_MISSING =
			new ParserError(77, Severity.ERROR, "'tests/onStates/stateRef' property is missing (Test [%s])"); 
	
	public static final ParserError STATEREF_DUPE =
			new ParserError(78, Severity.ERROR, "Duplicate property 'tests/onStates/stateRef' [%s] (Test [%s])"); 
	
	public static final ParserError STATEREF_UNDEFINED =
			new ParserError(79, Severity.ERROR, "'tests/onStates/stateRef' property [%s] references a state which does not exist (Test [%s])"); 
	
	public static final ParserError ALL_PROPER_EXPERIENCES_UNDEFINED =
			new ParserError(80, Severity.ERROR, "At least one proper state variant must be defined (Test [%s], StateRef [%s])"); 
	
	public static final ParserError ISNONVARIANT_NOT_BOOLEAN =
			new ParserError(81, Severity.ERROR, "'tests/onStates/isNonvariant' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_NOT_LIST =
			new ParserError(82, Severity.ERROR, "'tests/onStates/variants' property must be a list (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_LIST_EMPTY =
			new ParserError(83, Severity.ERROR, "'tests/onStates/variants' list must contain at least one element (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_UNSUPPORTED_PROPERTY =
			new ParserError(84, Severity.ERROR, "Unsupported property 'tests/onStates/variants/[%s]' (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_ISNONVARIANT_INCOMPATIBLE =
			new ParserError(85, Severity.ERROR, "Property 'tests/onStates' cannot be nonvariant and have variants (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANTS_ISNONVARIANT_XOR =
			new ParserError(86, Severity.ERROR, "Property 'tests/onStates' must specify one of: 'isNonvariant' or 'variants' (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANT_NOT_OBJECT =
			new ParserError(87, Severity.ERROR, "'tests/onStates/variants' list element must be an object (Test [%s], StateRef [%s])"); 
	
	public static final ParserError VARIANT_DUPE =
			new ParserError(88, Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references experience [%s] (Test [%s], StateRef [%s])"); 
	
	public static final ParserError COVARIANT_VARIANT_DUPE =
			new ParserError(89, Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references covariant experience(s) [%s] (Test [%s], StateRef [%s], Experience [%s])"); 
	
	public static final ParserError VARIANT_MISSING =
			new ParserError(90, Severity.ERROR, "Variant element 'tests/onStates/variants' missing for experience [%s] (Test [%s], StateRef [%s])"); 
	
	public static final ParserError COVARIANT_VARIANT_MISSING =
			new ParserError(91, Severity.ERROR, "'tests/onStates/variants' list element missing for proper experience [%s] and covariant experience(s) [%s] (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_VARIANT_TEST_NOT_COVARIANT =
			new ParserError(92, Severity.ERROR, "Variant element 'tests/onStates/variants' for covariant experience [%s.%s] cannot refer to a non-covariant test (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_VARIANT_PROPER_UNDEFINED =
			new ParserError(93, Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its proper experience is undefined (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_VARIANT_COVARIANT_UNDEFINED =
			new ParserError(94, Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its covariant experience [%s] is undefined (Test [%s], StateRef [%s])"); 	
	
	public static final ParserError COVARIANT_EXPERIENCEREFS_NOT_ALLOWED =
			new ParserError(95, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_PARAMS_NOT_ALLOWED =
			new ParserError(96, Severity.ERROR, "'tests/onStates/variants/parameters' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])");
	
	public static final ParserError COVARIANT_EXPERIENCEREFS_NOT_LIST =
			new ParserError(97, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property must be a list (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_REF_NOT_OBJECT =
			new ParserError(98, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element must be an object (Test [%s], StateRef [%s], ExperienceRefs [%s])"); 

	public static final ParserError COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT =
			new ParserError(99, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element cannot reference multiple tests, which are not covariant with each other (Test [%s], StateRef [%s], ExperienceRef [%s] CovariantExperienceRefs [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING =
			new ParserError(100, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING =
			new ParserError(101, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRefs' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED =
			new ParserError(102, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' references test [%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT =
			new ParserError(103, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' cannot reference test [%s] which is nonvariant on this state (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED =
			new ParserError(104, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRef' references a experience [%s.%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final ParserError COVARIANT_EXPERIENCE_DUPE =
			new ParserError(105, Severity.ERROR, "Duplicate list element 'tests/onStates/variants/covariantExperienceRefs' references experience [%s.%s] (Test [%s], StateRef [%s], Experience [%s])"); 
	
	public static final ParserError ISDEFINED_NOT_BOOLEAN =
			new ParserError(106, Severity.ERROR, "'tests/onStates/variants/isDefined' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_MISSING =
			new ParserError(107, Severity.ERROR, "'tests/onStates/variants/experienceRef' property is missing (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_NOT_STRING =
			new ParserError(108, Severity.ERROR, "'tests/onStates/variants/experienceRef' property must be a string (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_UNDEFINED =
			new ParserError(109, Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] references an expereince that does not exist (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_ISCONTROL =
			new ParserError(110, Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] cannot reference a control expereince, unless undefined (Test [%s], StateRef [%s])"); 
	
	public static final ParserError EXPERIENCEREF_PARAMS_NOT_OBJECT =
			new ParserError(111, Severity.ERROR, "'tests/onStates/variants/parameters' property must be an object (Test [%s], StateRef [%s], ExperienceRef [%s])");

	// 
	// 171-200 Schema parser Other
	//
	public static final ParserError JSON_PARSE =
			new ParserError(171, Severity.FATAL, "Invalid JSON syntax: [%s]");

	public static final ParserError UNSUPPORTED_CLAUSE =
			new ParserError(172, Severity.WARN,  "Unsupported clause [%s]");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected ParserError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
