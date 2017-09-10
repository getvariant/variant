package com.variant.server.impl;

import com.variant.core.UserHook;
import com.variant.server.lce.TestTargetingLifecycleEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.server.api.Session;

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

	@Override
	public Session getSession() {
		return session;
	}
	

}		