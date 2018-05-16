package com.variant.core.schema;

import java.util.List;

/**
 * Representation of the Test XDM element.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface Test {

	/**
	 * The schema that created this Test.
	 * 
	 * @return An object of type {@link Schema}
	 * @since 0.6
	 */
	public Schema getSchema();

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
	 * @return The test experience with this name or null, if none.
	 * @since 0.5
	 */
	public Experience getExperience(String name);
	
	/**
	 * This test's control experience.
	 * 
	 * @return The control experience.  Never null.
	 * @since 0.5
	 */
	public Experience getControlExperience();
	
	/**
	 * <p>Is this test currently on? Tests that are not on are treated specially:
	 * <ol>
     * <li>A user traversing this test will always see control experience.
     * <li>If a user already has an entry for this test in his targeting tracker, it will be ignored, 
     *     but not discarded. If not, no entries will be added to the user’s targeting tracker.
     * <li>No variant events will be logged for this test.
     * </ol>
	 * @return true if the test is on, false if not.
	 * @since 0.5
	 */
	public boolean isOn();
	
	/**
	 * <p>List of life-cycle hooks, defined in the scope of this test.
	 * 
	 * @return A list of {@link Hook} object in the ordinal order.
	 * @since 0.7
	 */
	public List<Hook> getHooks();

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
	public OnState getOnState(State state);
		
	/**
	 * Get a list of tests covariant with this test, i.e. listed in this tests's covariant clause.
	 * 
	 * @return A list of tests in the order they were defined or null if this test does not reference any covariant tests.
	 * @since 0.5
	 */
	public List<Test> getCovariantTests();
		
	/**
	 * Is this test serial with another test? Two tests are serial when there does not exist a state
	 * instrumented by both. Serial tests can be targeted independently without any extra work. 
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	public boolean isSerialWith(Test other);

	/**
	 * Is this test concurrent with the other test? This is equivalent to
	 * {@code !isSerialWith(other)}. Concurrent tests can only be targeted 
	 * independently if they are covariant.
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
		 * @return An object of type {@link Test}
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
		 * This experience's probabilistic weight. These are used by Variant's default
		 * test targeter if no custom targetting was provided.
		 * 
		 * @return Probabilistic weight, if declared.
	     * @since 0.5
		 */
		public Number getWeight();
		
		/**
		 * Is this experience defined on a given state?
		 * If a state variant is declared as undefined, it exclude that state from instrumentation
		 * by the corresponding proper experience. This comes in handy
		 * when a variant experience adds (or subtracts) states as compared with the control experience.
		 * See documentation for more on <i>mixed instrumentation</i>.
		 * 
		 * @return true if this variant was declared as defined, or false otherwise.
		 * @throws NullPointerException if argument is <code>null</code>
		 * @since 0.6
		 *
		*/
		public boolean isDefinedOn(State state);
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
		 * The state this instrumentation is for.
		 * 
		 * @return An object of type {@link State}
	     * @since 0.5
		 */
		public State getState();
		
		/**
		 * The test this instrumentation is on.
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
		 * @return A list of objects of type {@link StateVariant} or null if this instrumentation is non-variant.
	     * @since 0.5
		 */
		public List<StateVariant> getVariants();
		
	}
}
