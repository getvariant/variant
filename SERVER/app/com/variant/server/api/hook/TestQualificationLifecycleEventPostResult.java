package com.variant.server.api.hook;

import com.variant.core.UserHook;

///TODO
public interface TestQualificationLifecycleEventPostResult extends UserHook.PostResult {
	
	/**
	 * Host code calls this to inform Variant server whether the current session
	 * (as returned by {@link #getSession()}) is qualified for the test (as returned by {@link #getTest()}).
     *
	 * @param qualified Whether or not the session qualifies for the test.
	 * @since 0.7
	 */
	public void setQualified(boolean qualified);

	/**
	 * Host code calls this to inform Variant server whether the entry for this test should
	 * be removed from this session's targeting tracker, iff it is disqualified. The server
	 * will ignore the value set with this method, if this test was not disqualified. Conversely,
	 * if this test was disqualified, but this method was never called, the default value is false,
	 * which is to say that targeting tracker entries for disqualified tests are not discarded,
	 * unless provided by this method.
	 * 
	 * @param remove
	 * @since 0.7
	 */
public void setRemoveFromTargetingTracker(boolean remove);
}
