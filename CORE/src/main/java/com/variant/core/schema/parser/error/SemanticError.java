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

		@Override
		public boolean equals(Object other) {
			return (other instanceof Location)
					&& ((Location)other).path.equals(path);
		}
		
		/**
		 * New Location with extra object.
		 * @param extraPath
		 * @return
		 */
		public Location plusObj(String objPath) {		
			return new Location(path + objPath + "/");
		}
		
		/**
		 * New Location with extra property - no trailing slash.
		 * @param extraPath
		 * @return
		 */
		public Location plusProp(String propPath) {		
			return new Location(path + propPath);
		}

		/**
		 * Tack array index to the existing path. If path to array ends in /.
		 * insert the index before it. 
		 * @param index
		 * @return
		 */
		public Location plusIx(int index) {
			
			String newPath = path.endsWith("/") ? 
					newPath = path.substring(0, path.length()-1) + String.format("[%s]", index) + "/"
					: path + String.format("[%s]", index);
			
			return new Location(newPath);
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
	// 
	// 021-050 Schema parser, State
	//

	// 
	// 051-150 Schema parser, Variation
	//

	public static final SemanticError CONJOINT_TESTREF_UNDEFINED =
			new SemanticError(66, Severity.ERROR, "Property 'conjointVariationRefs' references non-existent variation [%s]"); 
	
	public static final SemanticError CONJOINT_TEST_SERIAL =
			new SemanticError(67, Severity.ERROR, "Variation [%s] cannot declar variation [%s] as conjoint because they are serial."); 
/*	
	public static final SemanticError ISCONTROL_NOT_BOOLEAN =
			new SemanticError(68, Severity.ERROR, "'tests/experience/isControl' property must be a boolean (Test [%s], Experience [%s])"); 
*/	
	public static final SemanticError CONTROL_EXPERIENCE_DUPE =
			new SemanticError(69, Severity.ERROR, "Duplicate control experience [%s] in variation [%s]"); 
	
	public static final SemanticError CONTROL_EXPERIENCE_MISSING =
			new SemanticError(70, Severity.ERROR, "Control experience is missing in variation [%s]"); 
/*	
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
*/	
	public static final SemanticError STATEREF_UNDEFINED =
			new SemanticError(79, Severity.ERROR, "Property 'stateRef' references non-existent state [%s]"); 
/*	
	public static final SemanticError ALL_PROPER_EXPERIENCES_UNDEFINED =
			new SemanticError(80, Severity.ERROR, "At least one proper state variant must be defined"); 

	public static final SemanticError ISNONVARIANT_NOT_BOOLEAN =
			new SemanticError(81, Severity.ERROR, "'tests/onStates/isNonvariant' property must be a boolean (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_NOT_LIST =
			new SemanticError(82, Severity.ERROR, "'tests/onStates/variants' property must be a list (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_LIST_EMPTY =
			new SemanticError(83, Severity.ERROR, "'tests/onStates/variants' list must contain at least one element (Test [%s], StateRef [%s])"); 
	
	public static final SemanticError VARIANTS_UNSUPPORTED_PROPERTY =
			new SemanticError(84, Severity.ERROR, "Unsupported property 'tests/onStates/variants/[%s]' (Test [%s], StateRef [%s])"); 

	public static final SemanticError VARIANTS_ISNONVARIANT_INCOMPATIBLE =
			new SemanticError(85, Severity.ERROR, "Non-variant state variant cannot have variants"); 
	
	public static final SemanticError VARIANTS_ISNONVARIANT_XOR =
			new SemanticError(86, Severity.ERROR, "An 'onStates' element must either be non-variant or have state variants"); 
	
	public static final SemanticError VARIANT_NOT_OBJECT =
			new SemanticError(87, Severity.ERROR, "'tests/onStates/variants' list element must be an object (Test [%s], StateRef [%s])"); 
*/	
	public static final SemanticError PROPER_VARIANT_DUPE =
			new SemanticError(88, Severity.ERROR, "Duplicate state variant references proper experience [%s]"); 
	
	public static final SemanticError CONJOINT_VARIANT_DUPE =
			new SemanticError(89, Severity.ERROR, "Duplicate state variant references conjoint experience(s) [%s]"); 
/*	
	public static final SemanticError PROPER_VARIANT_MISSING =
			new SemanticError(90, Severity.ERROR, "State variant missing for proper experience [%s]"); 
	
	public static final SemanticError CONJOINT_VARIANT_MISSING =
			new SemanticError(91, Severity.ERROR, "State variant missing for proper experience [%s] and conjoint experience(s) [%s]");
*/	
	public static final SemanticError CONJOINT_VARIANT_TEST_NOT_CONJOINT =
			new SemanticError(92, Severity.ERROR, "List element 'conjointExperienceRefs' cannot refer to non-conjoint variation [%s]"); 	
	
	public static final SemanticError CONJOINT_VARIANT_PROPER_PHANTOM =
			new SemanticError(93, Severity.ERROR, "State variant cannot refer to proper experience [%s], which is phantom on state [%s]"); 	
	
	public static final SemanticError CONJOINT_VARIANT_CONJOINT_PHANTOM =
			new SemanticError(94, Severity.ERROR, "State variant cannot refer to conjoint experience [%s], which is phantom on state [%s]"); 	
	
	public static final SemanticError PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT =
			new SemanticError(95, Severity.ERROR, "Property '%s' is not allowed in a phantom state variant"); 
	
	public static final SemanticError CONJOINT_EXPERIENCE_REF_TESTS_NOT_CONJOINT =
			new SemanticError(99, Severity.ERROR, "Property 'conjointExperienceRefs' cannot reference non-conjoint variations [%s]"); 

	public static final SemanticError CONJOINT_EXPERIENCE_TEST_REF_UNDEFINED =
			new SemanticError(102, Severity.ERROR, "Property 'variationRef' references non-existent variation [%s]"); 
/*
	public static final SemanticError CONJOINT_EXPERIENCE_TEST_REF_NONVARIANT =
			new SemanticError(103, Severity.ERROR, "Property 'variationRef' cannot reference variation [%s] because it is nonvariant on the enclosing variation [%s]"); 
*/
	public static final SemanticError CONJOINT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED =
			new SemanticError(104, Severity.ERROR, "Property 'experienceRef' references a non-existent experience [%s.%s]"); 
	
	public static final SemanticError CONJOINT_EXPERIENCE_DUPE =
			new SemanticError(105, Severity.ERROR, "Duplicate conjoint experience reference [%s.%s]"); 

	public static final SemanticError EXPERIENCEREF_UNDEFINED =
			new SemanticError(109, Severity.ERROR, "Property 'experienceRef' references a non-existent experience [%s]"); 
	
	public static final SemanticError EXPERIENCEREF_ISCONTROL =
			new SemanticError(110, Severity.ERROR, "Property 'experienceRef' cannot reference a control expereince"); 
	
	// 
	// 151-170 Schema parser Parameters
	//

	// 
	// 171-200 Schema parser Other
	//
	// Note that 171 is taken by the SyntaxError.JSON_SYTAX_ERROR.
	
	public static final SemanticError UNSUPPORTED_PROPERTY =
			new SemanticError(172, Severity.WARN,  "Unsupported property [%s]");

	public static final SemanticError NAME_INVALID =
			new SemanticError(173, Severity.ERROR, "Property 'name' must be a string, containing letters, digits and _, and cannot start with a digit"); 

	public static final SemanticError NAME_MISSING =
			new SemanticError(174, Severity.ERROR, "Property 'name' is missing"); 

	public static final SemanticError DUPE_OBJECT =
			new SemanticError(175, Severity.ERROR, "Object [%s] already defined"); 

	public static final SemanticError PROPERTY_NOT_LIST = 
			new SemanticError(176, Severity.ERROR, "Property '%s' must be a list"); 

	public static final SemanticError PROPERTY_NOT_OBJECT =
			new SemanticError(177, Severity.ERROR, "Property '%s' must be an object"); 

	public static final SemanticError ELEMENT_NOT_OBJECT =
			new SemanticError(178, Severity.ERROR, "Element of list property '%s' must be an object"); 

	public static final SemanticError PROPERTY_NOT_BOOLEAN =
			new SemanticError(179, Severity.ERROR, "Property '%s' must be a boolean"); 

	public static final SemanticError PROPERTY_NOT_STRING =
			new SemanticError(180, Severity.ERROR, "Property '%s' must be a string"); 

	public static final SemanticError ELEMENT_NOT_STRING =
			new SemanticError(181, Severity.ERROR, "Element of list property '%s' must be a string");
	
	public static final SemanticError PROPERTY_NOT_NUMBER =
			new SemanticError(182, Severity.ERROR, "Property '%s' must be a number"); 

	public static final SemanticError PROPERTY_EMPTY_LIST =
			new SemanticError(183, Severity.ERROR, "Property '%s' cannot be an empty list"); 
	
	public static final SemanticError PROPERTY_MISSING =
			new SemanticError(184, Severity.ERROR, "Property '%s' is missing"); 
	
}
