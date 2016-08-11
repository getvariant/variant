package com.variant.core.hook;

import com.variant.core.schema.Test;

/**
 * <p>Run time user hook that posts its listeners whenever a user session reaches a test
 * for the first time and need to be qualified for it. 
 * By default, any session is qualified for any test. Host code may change that by registering
 * a listener for this hook and calling {@link #setQualified(boolean)}.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface TestQualificationHook extends RuntimeHook {

	/**
	 * The test for which this user hook is posting.
	 * .
	 * @return An object of type {@link com.variant.core.schema.Test}.
	 * @since 0.5
	 */
	public Test getTest();
			
	/**
	 * Host code calls this to inform Variant server whether the current session
	 * (as returned by {@link #getSession()}) is qualified for the test (as returned by {@link #getTest()}).
     *
	 * @param qualified Whether or not the session qualifies for the test.
	 * @since 0.5
	 */
	public void setQualified(boolean qualified);

	/**
	 * Host code may obtain the current value set by the most recent call to {@link #setQualified(boolean)}.
	 * This is useful if host code registers multiple listeners for this hook and wants to know the value
	 * set by the previously posted listener. Listeners are posted in the order of registration.
	 * 
	 * @return Whether the session, as returned by {@link #getSession()}, is qualified for the test, 
	 *         as returned by {@link #getTest()}.
	 * @since 0.5
	 */
	public boolean isQualified();

	/**
	 * Host code calls this to inform Variant server whether the entry for this test should
	 * be removed from this session's targeting tracker, iff it is disqualified. The server
	 * will ignore the value set with this method, if this test was not disqualified. Conversely,
	 * if this test was disqualified, but this method was never called, the default value is false,
	 * which is to say that targeting tracker entries for disqualified tests are not discarded,
	 * unless provided by this method.
	 * 
	 * @param remove
	 * @since 0.5
	 */
	public void setRemoveFromTargetingTracker(boolean remove);

	/**
	 * Host code may obtain the current value set by the most recent call to {@link #setRemoveFromTargetingTracker(boolean)}.
	 * This is useful if host code registers multiple listeners for this hook and wants to know the value
	 * that was set by the previously posted listener. Listeners are posted in the order of registration.
	 * 
	 * @return Whether the targeting tracker entry for the test, as returned by {@link #getTest()}, is to be removed
	 *         from this session's, as returned by {@link #getSession()}, targeting tracker. 
	 *         
	 * @since 0.5
	 */
	public boolean isRemoveFromTargetingTracker();

}
