package com.variant.core.schema;

import java.util.List;
import java.util.Map;

/**
 * Representation of a test schema element.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface Test {

	/**
	 * The name of this test.
	 * 
	 * @return The name of this test.
	 * @since 0.5
	 */
	public String getName();	

	/**
	 * A list of this test's experiences in the order they were defined.
	 * 
	 * @return A list of objects of type {@link Test.Experience}.
	 * @since 0.5
	 */
	public List<Experience> getExperiences();
	
	/**
	 * This tests's experience by name. Experience names are case sensitive.
	 * 
	 * @param Experience name.
	 * @return The test experience with this name, if exists; null otherwise.
	 * @since 0.5
	 */
	public Experience getExperience(String name);
	
	/**
	 * This test's control experience. 
	 * 
	 * @return The control experience.
	 * @since 0.5
	 */
	public Experience getControlExperience();
	
	/**
	 * <p>Is this test currently on? Tests that are not on are treated specially:
	 * <ol>
     * <li>A user traversing this test will always see control experience.
     * <li>If a user already has an entry for this test in his targeting tracker, it will be ignored, but not discarded. Otherwise, no entries will be added to the user’s targeting tracker.
     * <li>No state served events will be logged on behalf of this test.
     * </ol>
	 * @return true if the test is on, false if not.
	 * @since 0.5
	 */
	public boolean isOn();
		
	/**
	 * List of all state instrumentations for this test, in the order they were defined.
	 * 
	 * @return A list of objects of type {@link OnState}.
	 * @since 0.5
	 */
	public List<OnState> getOnStates();

	/**
	 * The instrumentation for this test, on a particular state.
	 * 
	 * @return An object of type {@link OnState}, if exists, or null if given state is not instrumented by this test.
	 * @since 0.5
	 */
	public OnState getOnView(State state);
		
	/**
	 * Get a list of tests covariant with this test, i.e. listed in this tests's covariant clause.
	 * 
	 * @return A list of tests in the order they were defined.
	 * @since 0.5
	 */
	public List<Test> getCovariantTests();
		
	/**
	 * Is this test serial with the other test? Two tests are serial when there does not exist a state
	 * instrumented by both this and the other test. Serial tests can be targeted independently. 
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	public boolean isSerialWith(Test other);

	/**
	 * Is this test concurrent with the other test? This is equivalent to
	 * <code>!isSerialWith(other)</code>. 
	 * Concurrent tests can only be targeted independently if they are covariant.
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	public boolean isConcurrentWith(Test other);

	/**
	 * Is this test covariantly concurrent with the other test? Covariantly concurrent tests 
	 * can be targeted independently.
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	public boolean isCovariantWith(Test other);

	/**
	 * Representation of a test experience.
	 * 
	 * @since 0.5
	 */
	public interface Experience {

		/**
		 * The name of the experience.
		 * 
		 * @return The name of the experience.
	     * @since 0.5
		 */
		public String getName();
		
		/**
		 * The test this experience belongs to.
		 * 
		 * @return And object of type {@link Test}
	     * @since 0.5
		 */
		public Test getTest();
		
		/**
		 * Is this experience control for its test?
		 * 
		 * @return True if this experience is control for its test, false otherwise.
	     * @since 0.5
		 */
		public boolean isControl();
		
		/**
		 * This experience's random weight.  May be null.
		 * 
		 * @return
	     * @since 0.5
		 */
		public Number getWeight();
	}

	/**
	 * Representation of a test instrumentation on a particular state.
	 * Corresponds to an element of the test/onStates schema list.
	 * 
	 * @since 0.5
	 *
	 */
	public static interface OnState {
		
		/**
		 * The state this instrumentation is on.
		 * 
		 * @return An object of type {@link State}
	     * @since 0.5
		 */
		public State getState();
		
		/**
		 * The test this instrumentation is for.
		 * 
		 * @return An object of type {@link Test}
		 * @since 0.5
		 */
		public Test getTest();
		
		/**
		 * Is this instrumentation non-variant?
		 * 
		 * @return True if this instrumentation is non-variant, false otherwise.
	     * @since 0.5
		 */
		boolean isNonvariant();
		
		/**
		 * A list of all test variants for this instrumentation. 
		 * @return null if this is an nonvariant instrumentation.
	     * @since 0.5
		 */
		public List<Variant> getVariants();

		/**
		 * Representation of a single cell of a variance matrix.
		 * Corresponds to an element of the test/onStates/variants schema list.
		 *
	     * @since 0.5
		 */
		public static interface Variant {
			
			/**
			 * The {@link OnState} object this variant belongs to.
			 * 
			 * @return An object of type {@link OnState}.
			 * @since 0.5
			 */
			public OnState getOnState();

			/**
			 * The test for which this variant is defined. Equivalent to {@link #getOnState()}.getTest().
			 * 
			 * @return
			 * @since 0.5
			 */
			public Test getTest();
			
			/**
			 * This variant's own test experience, i.e. for the test within whose definition this variant
			 * is defined.
			 * 
			 * @return An object of type {@link Test.Experience}.
			 * @since 0.5
			 */
			public Experience getExperience();

			/**
			 * The list of this variant's covariantly concurrent experiences, i.e. the ones defined in the 
			 * covariant tests clause of the test within whose definition this variant is defined.
			 * 
			 * @return A list of objects of type {@link Test.Experience}.
			 * @since 0.5
			 */
			public List<Experience> getCovariantExperiences();
						
			/**
			 * This variant's state parameter map.
			 * @return
			 * @since 0.5
			 */
			public Map<String,String> getParameterMap();
		
		}	
	}
	
}
