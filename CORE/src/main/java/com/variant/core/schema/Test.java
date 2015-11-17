package com.variant.core.schema;

import java.util.List;
import java.util.Map;

/**
 * In-memory representation of a test.
 * @author Igor
 *
 */
public interface Test {

	/**
	 * Test name
	 * @return
	 */
	public String getName();	

	/**
	 * A list of all of this test's experiences in the order they were declared.
	 * @return
	 */
	public List<Experience> getExperiences();
	
	/**
	 * This tests's experience by name, case sensitive.
	 * @param name
	 * @return The test experience if exists, null otherwise.
	 */
	public Experience getExperience(String name);
	
	/**
	 * This test's control experience. 
	 * @return
	 */
	public Experience getControlExperience();
	
	/**
	 * Is this test currently on? Tests that are not on are treated especially:
	 * 1. Not targeted for.
	 * 2. Existing targeting is ignored, i.e. control experience is substituted for non-control targeting.
	 * 3. Events are not logged.
	 * @return
	 */
	public boolean isOn();
	
	/**
	 * Number of days an entry for this test will be kept by the targeting persister since the
	 * most recent traversal. 0 means forever - the default;
	 * @return
	 */
	public int getIdleDaysToLive();
	
	/**
	 * A list of all state instrumentations for this Test, in the order they were declared.
	 * @return
	 */
	public List<OnState> getOnStates();

	/**
	 * The view instrumentation for this Test, on a particular view.
	 * @return the OnView object or null if this test is not instrumented on this view.
	 */
	public OnState getOnView(State view);

	/**
	 * Is this test disjoint with the other test?
	 * This is to say that there does not exist a view on which both
	 * this and the other test are instrumented. It is illegal if this.equal(other) is true;
	 * 
	 * @param other
	 * @return
	 */
	public boolean isDisjointWith(Test other);
		
	/**
	 * Get a list of tests, declared by this test as covariant.
	 * 
	 * @return
	 */
	public List<Test> getCovariantTests();
	
	/**
	 * In-memory representation of a test experience.
	 * @author Igor
	 *
	 */
	public interface Experience {

		/**
		 * The name of the experience.
		 * @return
		 */
		public String getName();
		
		/**
		 * This experience's test.
		 * @return
		 */
		public Test getTest();
		
		/**
		 * Is this experience control for its test?
		 * @return
		 */
		public boolean isControl();
		
		/**
		 * This experience's declared weight.  May be null.
		 * @return
		 */
		public Number getWeight();
	}

	/**
	 * In-memory representation of a test instrumentation on a particular state.
	 * Corresponds to an element of the test/onStates configuration list.
	 * 
	 * @author Igor
	 *
	 */
	public static interface OnState {
		
		/**
		 * The state this instrumentation is on.
		 * @return
		 */
		public State getState();
		
		/**
		 * The test this instrumentation is for.
		 * @return
		 */
		public Test getTest();
		
		/**
		 * Is this instrumentation nonvariant, i.e. has no variants property.
		 * @return
		 */
		boolean isNonvariant();
		
		/**
		 * A list of all test variants for this instrumentation. 
		 * @return null if this is an nonvariant instrumentation.
		 */
		public List<Variant> getVariants();

		/**
		 * In-memory representation of an instrumentation variant.
		 * Corresponds to an element of the test/onStates/variants configuration list.
		 *
		 */
		public static interface Variant {
			
			/**
			 * The Test.OnState object this Variant belongs to.
			 * @return
			 */
			public OnState getOnState();

			/**
			 * The test within which this variant is defined. Equivalent to calling
			 * <code>getOnState().getTest()</code>
			 * @return
			 */
			public Test getTest();
			
			/**
			 * This list's own experience, defined in its own test.
			 * @return
			 */
			public Experience getExperience();

			/**
			 * List of this variant's covariant experiences, i.e. the ones defined in covariant tests.
			 * 
			 * @return
			 */
			public List<Experience> getCovariantExperiences();
						
			/**
			 * Resource path corresponding to this test variant.
			 * @return
			 */
			public Map<String,String> getParameterMap();
		
		}	
	}
	
}
