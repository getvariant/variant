package com.variant.server.api.hook;

import com.variant.core.UserHook;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * <p>Run time hook. Posts its listeners whenever a user session is about to be targeted 
 * for a session, but after all qualification hooks have fired and the qualification of 
 * this session for this test has been determined. If no listeners are supplied, Variant
 * server targets the session randomly, according to experience weights supplied in the schema. 
 * If a listener wishes to override this default, it may call 
 * {@link #setTargetedExperience(Test.Experience)}. 
 * 
 * <p><b>It is important to understand that if custom targeting decision is not pseudo-random, then the 
 * outcome of such experiment may not be statistically sound: there might be some
 * reason other than the difference in treatment, that explains the difference in measurements.</b>
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface TestTargetingLifecycleEvent extends StateRequestAwareLifecycleEvent, TestScopedLifecycleEvent {
	
	/**
	 * The state on which this user hook is posting.
	 * 
	 * @return An object of type {@link State}.
	 * @since 0.6
	 */
	public State getState();

	///TODO
	public interface PostResult extends UserHook.PostResult {
	   
	   /**
	    * Host code calls this to inform Variant what experience should the session,
	    * returned by {@link #getSession()}, be targeted for in the test returned by {@link #getTest()}.
	    * If host code never calls this method, the initial value is null, which Variant server will
	    * interpret by falling back on the default random targeting algorithm based on the probability
	    * weights in the tests's definition.
	    * .
	    * @param experience Targeted experience.
	    * @throws VariantRuntimeUserErrorException if experience is not that of the test returned by 
	    *         {@link #getTest()}.
	    * @since 0.7
	    */
	   public void setTargetedExperience(Experience experience);   

	}

}
