package com.variant.server.impl;

import com.variant.core.UserHook;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.server.api.Session;
import com.variant.server.api.StateRequest;
import com.variant.server.api.hook.TestTargetingLifecycleEvent;

/**
 * 
 */
public class TestTargetingLifecycleEventImpl implements TestTargetingLifecycleEvent {

	private Session session;
	private Test test;
	private State state;
	
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
	public Test getTest() {
		return test;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public UserHook<TestTargetingLifecycleEvent> getDefaultHook() {
		
		return new TestTargetingDefaultHook();
	}
	

}		
