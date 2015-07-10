package com.variant.core.schema;

import java.util.List;

/**
 * In-memory representation of a test.
 * @author Igor
 *
 */
public interface Test {

	/**
	 * View's declared name
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
	 * A list of all views instrumented for this Test, in the order they were declared.
	 * @return
	 */
	public List<OnView> getOnViews();

	/**
	 * Get a list of tests covariant with this test.  
	 * Each test in this list is either mentioned in this test's covariant test refs,
	 * or mentions this test in its covaraint test refs.  In other words, relationship
	 * of covariance is commutative: if A is covariant with B, then B is also covariant
	 * with A.
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
		 * This experience's declared weight.
		 * @return
		 */
		public float getWeight();
	}

	/**
	 * In-memory representation of a test instrumentation on a particular view.
	 * Corresponds to an element of the test/onViews configuration list.
	 * 
	 * @author Igor
	 *
	 */
	public static interface OnView {
		
		/**
		 * The view this instrumentation is on.
		 * @return
		 */
		public View getView();
		
		/**
		 * The test this instrumentation is for.
		 * @return
		 */
		public Test getTest();
		
		/**
		 * Is this instrumentation invariant, i.e. has no variants property.
		 * @return
		 */
		boolean isInvariant();
		
		/**
		 * A list of all test variants for this instrumentation. 
		 * @return null if this is an invariant instrumentation.
		 */
		public List<Variant> getVariants();

		/**
		 * In-memory representation of an instrumentation variant.
		 * Corresponds to an element of the test/onViews/variants configuration list.
		 * 
		 * @author Igor
		 *
		 */
		public static interface Variant {
			
			/**
			 * Test experience this variant is for.
			 * @return
			 */
			public Experience getExperience();
			
			/**
			 * Resource path corresponding to this test variant.
			 * @return
			 */
			public String getPath();
		}
	}

}
