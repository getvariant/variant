package com.variant.core.hook;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * <p>Run time user hook that is reached whenever a user session encounters a test for the first time
 * for which there is no targeting information in the targeting tracker. By default, the container will
 * target a session to a test randomly, according to the probability weights in the test's definition. 
 * Client code may change that by getting posted of this hook and informing the container whether 
 * of a custom targeting decision. It is important to understand that if custom targeting decision is
 * not random, the outcome of such experiment is likely not statistically sound.
 * 
 * @author Igor.
 * @since 0.5.1
 *
 */
public interface TestTargetingHook extends RuntimeHook {

	/**
	 * Client code may obtain the Test for which this user hook was reached.
	 * .
	 * @return An object of type {@link com.variant.core.schema.Test}.
	 * @since 0.5.1
	 */
	public Test getTest();
	
	/**
	 * Client code calls this to inform the container what experience should the session
	 * as returned by {@link #getSession()} be targeted for in the test returned by {@link #getTest()}.
	 * If client code never calls this method, the initial value is null, which the container will
	 * interpret by falling back on the default random targeting algorithm based on the probability
	 * weights in the tests's definition.
	 * .
	 * @param experience Targeted experience.
	 * @since 0.5.1
	 */
	public void setTargetedExperience(Experience experience);

	/**
	 * Client code may obtain the current value set by the most recent call to {@link #setTargetedExperience(Experience)}.
	 * This is useful if client code registers multiple listeners for this hook and wants to know the value
	 * that was set by the previously posted listener.
	 * 
	 * @return Currently targeted experience, if any.
	 * @since 0.5.1
	 */
	public Experience getTargetedExperience();
	
}
