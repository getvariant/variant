package com.variant.server.api.hook;

import com.variant.core.UserHook;
import com.variant.core.schema.Test.Experience;

///TODO
public interface TestTargetingLifecycleEventPostResult extends UserHook.PostResult {
	
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
