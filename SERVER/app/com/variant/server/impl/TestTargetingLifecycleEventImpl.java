package com.variant.server.impl;

import static com.variant.server.boot.ServerErrorLocal.HOOK_TARGETING_BAD_EXPERIENCE;

import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.server.api.ServerException;
import com.variant.server.api.Session;
import com.variant.server.api.StateRequest;
import com.variant.server.api.TestTargetingLifecycleEvent;

/**
 * 
 */
public class TestTargetingLifecycleEventImpl implements TestTargetingLifecycleEvent {

	private Session session;
	private Test test;
	private State state;
	private Experience targetedExperience = null;
	
	public TestTargetingLifecycleEventImpl(Session session, Test test, State state) {
		this.session = session;
		this.test = test;
		this.state = state;
	}
	
	@Override
	public StateRequest getStateRequest() {
		return session.getStateRequest();
	}

	@Override
	public Experience getTargetedExperience() {
		return targetedExperience;
	}

	@Override
	public Test getTest() {
		return test;
	}

	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public void setTargetedExperience(Experience e) {
		
		if (e == null) {
			targetedExperience = null;
			return;
		}
		
		for (Experience te: test.getExperiences()) {
			if (e.equals(te)) {
				if (!e.isDefinedOn(state)) {
					StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
					throw new ServerException.User(
							HOOK_TARGETING_BAD_EXPERIENCE, 
							caller.getClassName(), test.getName(), e.toString(), test.getName());
				}
				targetedExperience = e;
				return;
			}
		}
		// If we're here, the experience is not from the test we're listening for.
		// Figure out the caller class and throw an exception.
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		throw new ServerException.User(
				HOOK_TARGETING_BAD_EXPERIENCE, 
				caller.getClassName(), test.getName(), e.toString());
	}

}		
