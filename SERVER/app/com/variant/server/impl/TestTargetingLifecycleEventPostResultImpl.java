package com.variant.server.impl;

import static com.variant.core.ServerError.HOOK_TARGETING_BAD_EXPERIENCE;

import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.server.api.ServerException;
import com.variant.server.api.lifecycle.TestTargetingLifecycleEvent;

public class TestTargetingLifecycleEventPostResultImpl implements TestTargetingLifecycleEvent.PostResult {

	TestTargetingLifecycleEventImpl event;
	private Experience experience = null;
	
	/**
	 * 
	 * @param event
	 */
	public TestTargetingLifecycleEventPostResultImpl(TestTargetingLifecycleEvent event) {
		this.event = (TestTargetingLifecycleEventImpl) event;
	}
	
	@Override
	public void setTargetedExperience(Experience experience) {

		Test test = event.getTest();
		State state = event.getState();
		
		for (Experience te: test.getExperiences()) {
			if (experience.equals(te)) {
				if (experience.isPhantomOn(state)) {
					StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
					throw new ServerException.Local(
							HOOK_TARGETING_BAD_EXPERIENCE, 
							caller.getClassName(), test.getName(), experience.toString(), test.getName());
				}
				this.experience = experience;
				return;
			}
		}
		// If we're here, the experience is not from the test we're listening for.
		// Figure out the caller class and throw an exception.
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		throw new ServerException.Local(
				HOOK_TARGETING_BAD_EXPERIENCE, 
				caller.getClassName(), test.getName(), experience.toString());
	}

	public Experience getTargetedExperience() {
		return experience;
	}

}
