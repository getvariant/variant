package com.variant.core.hook;

import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

/**
 * <p>Run time user hook that posts its listeners whenever a user session must be targeted for a test. 
 * By default, the Variant server will target a session to a test randomly, according to the probability
 * weights in the test's definition. Client code may change that by registering a listener for this hook
 * and calling {@link #setTargetedExperience(Experience)}. 
 * 
 * <p><b>It is important to understand that if custom targeting decision is not pseudo-random, then the 
 * outcome of such experiment may not be statistically sound: there might be some
 * reason other than the difference in experiences, that explains the difference in measurements.</b>
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface TestTargetingHook extends RuntimeHook {

	/**
	 * The Test for which this user hook is posting.
	 * .
	 * @return An object of type {@link com.variant.core.xdm.Test}.
	 * @since 0.5
	 */
	public Test getTest();
	
	/**
	 * Host code calls this to inform Variant what experience should the session,
	 * returned by {@link #getSession()}, be targeted for in the test returned by {@link #getTest()}.
	 * If host code never calls this method, the initial value is null, which Variant server will
	 * interpret by falling back on the default random targeting algorithm based on the probability
	 * weights in the tests's definition.
	 * .
	 * @param experience Targeted experience.
	 * @since 0.5
	 */
	public void setTargetedExperience(Experience experience);

	/**
	 * Host code may obtain the current value set by the most recent call to {@link #setTargetedExperience(Experience)}.
	 * This is useful if host code registers multiple listeners for this hook and wants to know the value
	 * that was set by the previously posted listener. Listeners are posted in the order of registration.
	 * 
	 * @return Currently targeted experience, if any.
	 * @since 0.5
	 */
	public Experience getTargetedExperience();
	
}
