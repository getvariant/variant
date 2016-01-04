package com.variant.core.hook;

import com.variant.core.schema.Test;

/**
 * <p>Run time user hook that is reached whenever a user session encounters a test for the first time.
 * By default, any session is qualified for any test it encounters. Client code may change that by
 * getting posted of this hook and informing the container whether this session is in fact qualified
 * for a particular test.
 * 
 * @author Igor.
 * @since 0.5.1
 *
 */
public interface TestQualificationHook extends RuntimeHook {

	/**
	 * Client code may obtain the Test for which this user hook was reached.
	 * .
	 * @return An object of type {@link com.variant.core.schema.Test}.
	 * @since 0.5.1
	 */
	public Test getTest();
			
	/**
	 * Client code calls this to inform the container whether the current session
	 * as returned by {@link #getSession()} is qualified for the test returned by {@link #getTest()}.
	 * If client code never calls this method, the initial value is true.
     *
	 * @param qualified
	 * @since 0.5
	 */
	public void setQualified(boolean qualified);

	/**
	 * Client code may obtain the current value set by the most recent call to {@link #setQualified(boolean)}.
	 * This is useful if client code registers multiple listeners for this hook and wants to know the value
	 * that was set by the previously posted listener.
	 * 
	 * @return Current qualification.
	 * @since 0.5.1
	 */
	public boolean isQualified();

	/**
	 * Client code calls this to inform the container whether the entry for this test should
	 * be removed from this session's targeting persister, iff it is disqualified. The container
	 * will ignore the value set with this method, if this test was not disqualified. Conversely,
	 * if this test was disqualified, but this method was never called, the initial value is false,
	 * which is to say that targeting tracker entries for disqualified tests are not discarded by 
	 * default.
	 * 
	 * @param remove
	 * @since 0.5.1
	 */
	public void setRemoveFromTargetingTracker(boolean remove);

	/**
	 * Client code may obtain the current value set by the most recent call to {@link #setRemoveFromTargetingTracker(boolean)}.
	 * This is useful if client code registers multiple listeners for this hook and wants to know the value
	 * that was set by the previously posted listener.
	 * 
	 * @return Current qualification.
	 * @since 0.5.1
	 */
	public boolean isRemoveFromTargetingTracker();

}
