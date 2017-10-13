package com.variant.core.schema.parser.error;


public class SemanticError extends ParserError {
	
	/**
	 */
	protected SemanticError(int code, Severity severity, String format) {
		super(code, severity, format);
	}
	
	/**
	 * Base abstract class for all semantic error locations.
	 * 
	 * @author Igor
	 */
	public static class Location implements com.variant.core.schema.ParserMessage.Location {

		private final String path;
		
		public Location(String path) {
			this.path = path;
		}

		/**
		 * Subclasses will impelemnt this.
		 */
		@Override
		public String getPath() {
			return path;
		}

		@Override
		public String toString() {
			return getPath();
		}

		/**
		 * New SemanticErrorLocation with the extraPath added to the path of this.
		 * @param extraPath
		 * @return
		 */
		public Location plus(String extraPath) {		
			return new Location(getPath() + "/" + extraPath);
		}
		
		/**
		 * Same, but array index.
		 * @param index
		 * @return
		 */
		public Location plus(int index) {
			return plus(String.format("[%s]", index));
		}

	}

	/**
	 * Semantic parse errors.
	 * @param args
	 * @return
	 */
	public String asMessage(Location location, Object...msgArgs) {
		return String.format(msgFormat, msgArgs) + "\nLocation: " + location.getPath();
	}

	// 
	// 001-020 Schema parser, Meta 
	//
	public static final SemanticError NO_META_CLAUSE =
			new SemanticError(1, Severity.ERROR,  "'/meta' clause is missing");

	public static final SemanticError META_NOT_OBJECT =
			new SemanticError(2, Severity.ERROR, "'/meta' property must be an object"); 

	public static final SemanticError META_NAME_INVALID =
			new SemanticError(3, Severity.ERROR, "Schema name must be a string, containing letters, digits and _, and cannot start with a digit"); 

	public static final SemanticError META_NAME_MISSING =
			new SemanticError(4, Severity.ERROR, "Schema name is missing"); 

	public static final SemanticError META_COMMENT_INVALID =
			new SemanticError(5, Severity.ERROR, "Schema comment must be a string"); 

	public static final SemanticError META_UNSUPPORTED_PROPERTY =
			new SemanticError(6, Severity.WARN,  "Unsupported property 'meta/%s'"); 

	public static final SemanticError HOOK_NAME_INVALID =
			new SemanticError(7, Severity.ERROR, "Hook name must be a string, containing letters, digits and _, and cannot start with a digit"); 

	public static final SemanticError HOOK_CLASS_NAME_INVALID =
			new SemanticError(8, Severity.ERROR, "Hook class name must be a string (hook [%s]"); 
	
	public static final SemanticError HOOK_UNSUPPORTED_PROPERTY =
			new SemanticError(9, Severity.WARN,  "Unsupported hook property [%s] (hook [%s])"); 

	public static final SemanticError HOOK_NAME_MISSING =
			new SemanticError(10, Severity.ERROR, "Hook name missing"); 

	public static final SemanticError HOOK_CLASS_NAME_MISSING =
			new SemanticError(11, Severity.ERROR, "Hook class name missing for hook [%s]"); 

	public static final SemanticError HOOK_NAME_DUPE =
			new SemanticError(12, Severity.ERROR, "Duplicate hook name [%s]"); 

	public static final SemanticError HOOKS_NOT_LIST = 
			new SemanticError(13, Severity.ERROR, "'hooks' property must be a list"); 

	public static final SemanticError HOOK_NOT_OBJECT =
			new SemanticError(14, Severity.ERROR, "'meta/hooks' property element must be an object"); 

	public static final SemanticError FLUSHER_NOT_OBJECT =
			new SemanticError(15, Severity.ERROR, "'meta/flusher' property must be an object"); 

	public static final SemanticError FLUSHER_CLASS_NAME_INVALID =
			new SemanticError(16, Severity.ERROR, "Flusher class name must be a string"); 

	public static final SemanticError FLUSHER_UNSUPPORTED_PROPERTY =
			new SemanticError(17, Severity.WARN,  "Unsupported flusher property [%s]"); 

	public static final SemanticError FLUSHER_CLASS_NAME_MISSING =
			new SemanticError(18, Severity.ERROR, "Flusher class name missing"); 

	// 
	// 021-050 Schema parser, State
	//
	public static final SemanticError STATE_UNSUPPORTED_PROPERTY =
			new SemanticError(21, Severity.WARN,  "Unsupported state property [%s] (State [%s])"); 

	public static final SemanticError NO_STATES_CLAUSE =
			new SemanticError(22, Severity.INFO,  "'states' clause is missing");
	
	public static final SemanticError STATES_CLAUSE_NOT_LIST = 
			new SemanticError(23, Severity.ERROR, "'/states' element must be a list"); 
	
	public static final SemanticError STATES_CLAUSE_EMPTY = 
			new SemanticError(24, Severity.INFO, "'states' clause is empty"); 

	public static final SemanticError STATE_NAME_MISSING =
			new SemanticError(25, Severity.ERROR, "State name is missing"); 
	
	public static final SemanticError STATE_NAME_INVALID =
			new SemanticError(26, Severity.ERROR, "State name must be a string, containing letters, digits and _, and cannot start with a digit"); 
	
	public static final SemanticError STATE_NAME_DUPE =
			new SemanticError(27, Severity.ERROR, "Duplicate state name [%s]"); 
	
	// 
	// 051-150 Schema parser, Test
	//
	public static final SemanticError NO_TESTS_CLAUSE =
			new SemanticError(51, Severity.INFO,  "'/tests' clause is missing"); 
	
	public static final SemanticError NO_TESTS =
			new SemanticError(52, Severity.INFO,  "No tests found"); 
	
	public static final SemanticError TEST_NAME_MISSING =
			new SemanticError(53, Severity.ERROR, "Test name is missing"); 
	
	public static final SemanticError TEST_NAME_INVALID =
			new SemanticError(54, Severity.ERROR, "Test name must be a string, containing letters, digits and _, and cannot start with a digit"); 
	
	public static final SemanticError TEST_NAME_DUPE =
			new SemanticError(55, Severity.ERROR, "Duplicate test name [%s]"); 
	
	public static final SemanticError TEST_ISON_NOT_BOOLEAN =
			new SemanticError(56, Severity.ERROR, "'tests/isOn' property must be a boolean (Test [%s])"); 
	
	public static final SemanticError TEST_UNSUPPORTED_PROPERTY =
			new SemanticError(57, Severity.WARN,  "Unsupported property 'tests/%s' (Test [%s])"); 
	
	public static final SemanticError EXPERIENCES_NOT_LIST =
			new SemanticError(58, Severity.ERROR, "'tests/experiences' property must be a list (Test [%s])"); 
	
	public static final SemanticError EXPERIENCES_LIST_EMPTY =
			new SemanticError(59, Severity.ERROR, "'tests/experiences' list must contain at least one element (Test [%s])"); 
	
	public static final SemanticError EXPERIENCE_NOT_OBJECT =
			new SemanticError(60, Severity.ERROR, "'tests/experiences' list element must be an object (Test [%s])");  
	
	public static final SemanticError EXPERIENCE_NAME_MISSING =
			new SemanticError(61, Severity.ERROR, "Experience name is missing (Test [%s])"); 
	
	public static final SemanticError EXPERIENCE_NAME_INVALID =
			new SemanticError(62, Severity.ERROR, "Experience name must be a string containing letters, digits and _, and cannot start with a digit (Test [%s])"); 
	
	public static final SemanticError EXPERIENCE_NAME_DUPE =
			new SemanticError(63, Severity.ERROR, "Duplicate expereince name [%s] in test [%s]"); 
	
	public static final SemanticError COVARIANT_TESTS_NOT_LIST =
			new SemanticError(64, Severity.ERROR, "'tests/covariantTestRefs' property must be a list (Test [%s])"); 
	
	public static final SemanticError COVARIANT_TESTREF_NOT_STRING =
			new SemanticError(65, Severity.ERROR, "'tests/covariantTestRefs' list element must be a string (Test [%s])"); 
	
	public static final SemanticError COVARIANT_TESTREF_UNDEFINED =
			new SemanticError(66, Severity.ERROR, "Property 'tests/covariantTestRefs' references test [%s], which does not exist (Test [%s])"); 
	
	public static final SemanticError COVARIANT_TEST_DISJOINT =
			new SemanticError(67, Severity.ERROR, "Covariant test [%s] cannot be disjoint (Test [%s])"); 
	
	public static final SemanticError ISCONTROL_NOT_BOOLEAN =
			new SemanticError(68, Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"); 
	
	public static final SemanticError CONTROL_EXPERIENCE_DUPE =
			new SemanticError(69, Severity.ERROR, "Duplicate control experience [%s] in test [%s]"); 
	
	public static final SemanticError CONTROL_EXPERIENCE_MISSING =
			new SemanticError(70, Severity.ERROR, "Control experience is missing in test [%s]"); 
	
	public static final SemanticError WEIGHT_NOT_NUMBER =
			new SemanticError(71, Severity.ERROR, "'tests/experience/weight' property must be a number (Test [%s], Experience [%s])"); 
	
	public static final SemanticError EXPERIENCE_UNSUPPORTED_PROPERTY =
			new SemanticError(72, Severity.WARN,  "Unsupported property 'test/experience/%s' (Test [%s], Experience [%s])"); 
	
	public static final SemanticError ONSTATES_NOT_LIST =
			new SemanticError(73, Severity.ERROR, "'tests/onStates' property must be a list (Test [%s])"); 
	
	public static final SemanticError ONSTATES_LIST_EMPTY =
			new SemanticError(74, Severity.ERROR, "'tests/onStates' list must contain at least one element (Test [%s])"); 
	
	public static final SemanticError ONSTATES_NOT_OBJECT =
			new SemanticError(75, Severity.ERROR, "'tests/onStates' list element must be an object (Test [%s])"); 
	
	public static final SemanticError STATEREF_NOT_STRING =
			new SemanticError(76, Severity.ERROR, "'tests/onStates/stateRef' property must be a string (Test [%s])"); 
	
	public static final SemanticError STATEREF_MISSING =
			new SemanticError(77, Severity.ERROR, "'tests/onStates/stateRef' property is missing (Test [%s])"); 
	
	public static final SemanticError STATEREF_DUPE =
			new SemanticError(78, Severity.ERROR, "Duplicate property 'tests/onStates/stateRef' [%s] (Test [%s])"); 
	
	public static final SemanticError STATEREF_UNDEFINED =
			new SemanticError(79, Severity.ERROR, "'tests/onStates/stateRef' property [%s] references a state which does not exist (Test [%s])"); 
	
	public static final SemanticError ALL_PROPER_EXPERIENCES_UNDEFINED =
			new SemanticError(80, Severity.ERROR, "At least one proper state variant must be defined (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError ISNONVARIANT_NOT_BOOLEAN =
			new SemanticError(81, Severity.ERROR, "'tests/onStates/isNonvariant' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_NOT_LIST =
			new SemanticError(82, Severity.ERROR, "'tests/onStates/variants' property must be a list (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_LIST_EMPTY =
			new SemanticError(83, Severity.ERROR, "'tests/onStates/variants' list must contain at least one element (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_UNSUPPORTED_PROPERTY =
			new SemanticError(84, Severity.ERROR, "Unsupported property 'tests/onStates/variants/[%s]' (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_ISNONVARIANT_INCOMPATIBLE =
			new SemanticError(85, Severity.ERROR, "Property 'tests/onStates' cannot be nonvariant and have variants (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_ISNONVARIANT_XOR =
			new SemanticError(86, Severity.ERROR, "Property 'tests/onStates' must specify one of: 'isNonvariant' or 'variants' (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANT_NOT_OBJECT =
			new SemanticError(87, Severity.ERROR, "'tests/onStates/variants' list element must be an object (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANT_DUPE =
			new SemanticError(88, Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references experience [%s] (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError COVARIANT_VARIANT_DUPE =
			new SemanticError(89, Severity.ERROR, "Duplicate list element 'tests/onStates/variants' references covariant experience(s) [%s] (Test [%s], StateRef [%s], Experience [%s])"); 
	
	public static final SemanticError VARIANT_MISSING =
			new SemanticError(90, Severity.ERROR, "Variant element 'tests/onStates/variants' missing for experience [%s] (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError COVARIANT_VARIANT_MISSING =
			new SemanticError(91, Severity.ERROR, "'tests/onStates/variants' list element missing for proper experience [%s] and covariant experience(s) [%s] (Test [%s], StateRef [%s])"); 	
	
	public static final SemanticError COVARIANT_VARIANT_TEST_NOT_COVARIANT =
			new SemanticError(92, Severity.ERROR, "Variant element 'tests/onStates/variants' for covariant experience [%s.%s] cannot refer to a non-covariant test (Test [%s], StateRef [%s])"); 	
	
	public static final SemanticError COVARIANT_VARIANT_PROPER_UNDEFINED =
			new SemanticError(93, Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its proper experience is undefined (Test [%s], StateRef [%s])"); 	
	
	public static final SemanticError COVARIANT_VARIANT_COVARIANT_UNDEFINED =
			new SemanticError(94, Severity.ERROR, "'tests/onStates/variants' list element for proper experience [%s] and covariant experience(s) [%s] is invalid because its covariant experience [%s] is undefined (Test [%s], StateRef [%s])"); 	
	
	public static final SemanticError COVARIANT_EXPERIENCEREFS_NOT_ALLOWED =
			new SemanticError(95, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError EXPERIENCEREF_PARAMS_NOT_ALLOWED =
			new SemanticError(96, Severity.ERROR, "'tests/onStates/variants/parameters' property not allowed in an undefined variant (Test [%s], StateRef [%s], ExperienceRef [%s])");
	
	public static final SemanticError COVARIANT_EXPERIENCEREFS_NOT_LIST =
			new SemanticError(97, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' property must be a list (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_REF_NOT_OBJECT =
			new SemanticError(98, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element must be an object (Test [%s], StateRef [%s], ExperienceRefs [%s])"); 

	public static final SemanticError COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT =
			new SemanticError(99, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs' list element cannot reference multiple tests, which are not covariant with each other (Test [%s], StateRef [%s], ExperienceRef [%s] CovariantExperienceRefs [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING =
			new SemanticError(100, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING =
			new SemanticError(101, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRefs' property must be a string (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED =
			new SemanticError(102, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' references test [%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT =
			new SemanticError(103, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/testRef' cannot reference test [%s] which is nonvariant on this state (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED =
			new SemanticError(104, Severity.ERROR, "'tests/onStates/variants/covariantExperienceRefs/experienceRef' references a experience [%s.%s], which does not exist (Test [%s], StateRef [%s], ExperienceRef [%s])"); 
	
	public static final SemanticError COVARIANT_EXPERIENCE_DUPE =
			new SemanticError(105, Severity.ERROR, "Duplicate list element 'tests/onStates/variants/covariantExperienceRefs' references experience [%s.%s] (Test [%s], StateRef [%s], Experience [%s])"); 
	
	public static final SemanticError ISDEFINED_NOT_BOOLEAN =
			new SemanticError(106, Severity.ERROR, "'tests/onStates/variants/isDefined' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError EXPERIENCEREF_MISSING =
			new SemanticError(107, Severity.ERROR, "'tests/onStates/variants/experienceRef' property is missing (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError EXPERIENCEREF_NOT_STRING =
			new SemanticError(108, Severity.ERROR, "'tests/onStates/variants/experienceRef' property must be a string (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError EXPERIENCEREF_UNDEFINED =
			new SemanticError(109, Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] references an expereince that does not exist (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError EXPERIENCEREF_ISCONTROL =
			new SemanticError(110, Severity.ERROR, "'tests/onStates/variants/experienceRef' property [%s] cannot reference a control expereince, unless undefined (Test [%s], StateRef [%s])"); 
	
	// 
	// 151-170 Schema parser Parameters
	//
	public static final SemanticError PARAMS_NOT_LIST = 
			new SemanticError(151, Severity.ERROR, "'parameters' property must be a list"); 

	public static final SemanticError PARAM_NOT_OBJECT =
			new SemanticError(152, Severity.ERROR, "'parameters' property element must be an object"); 
	
	public static final SemanticError PARAM_NAME_INVALID =
			new SemanticError(153, Severity.ERROR, "Parameter name must be a string, containing letters, digits and _, and cannot start with a digit"); 

	public static final SemanticError PARAM_VALUE_INVALID =
			new SemanticError(154, Severity.ERROR, "Parameter value must be a string"); 

	public static final SemanticError PARAM_NAME_MISSING =
			new SemanticError(155, Severity.ERROR, "Parameter name missing"); 

	public static final SemanticError PARAM_VALUE_MISSING =
			new SemanticError(156, Severity.ERROR, "Parameter value missing"); 

	// 
	// 171-200 Schema parser Other
	//
	public static final SemanticError UNSUPPORTED_CLAUSE =
			new SemanticError(172, Severity.WARN,  "Unsupported clause [%s]");
	
}
