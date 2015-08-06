package com.variant.core.schema;

import java.util.List;

import com.variant.core.VariantSession;

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
	 * A list of all view instrumentations for this Test, in the order they were declared.
	 * @return
	 */
	public List<OnView> getOnViews();

	/**
	 * The view instrumentation for this Test, on a particular view.
	 * @return the OnView object or null if this test is not instrumented on this view.
	 */
	public OnView getOnView(View view);

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
	 * Register a custom targeter.  Multiple targeters are executed in the order order
	 * they were registered.
	 * 
	 * @param targeter
	 */
	public void registerCustomTargeter(Targeter targeter);
	
	/**
	 * Get the list of custom targeters in the order they were defined.
	 * @return
	 */
	public List<Targeter> getCustomTargeters();
	
	/**
	 * Remove all custom targeters.
	 */
	public void clearCustomTargeters();

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
		public double getWeight();
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
			 * The Test.OnView object this Variant belongs to.
			 * @return
			 */
			public OnView getOnView();

			/**
			 * The test within which this variant is defined. Equivalent to calling
			 * <code>getOnView().getTest()</code>
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
			public String getPath();
		}
	}
	
	/**
	 * Custom targeter implements this interface.
	 * @author Igor
	 * 
	 */
	public static interface Targeter {
		
		/**
		 * 
		 * @param session
		 * @return null to defer to the next targeter on the chain, or an experience.
		 */
		public Experience target(Test test, VariantSession session);
	}

}
