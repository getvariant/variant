package com.variant.server.impl;

import com.variant.core.UserHook;
import com.variant.server.lce.TestQualificationLifecycleEvent;
import com.variant.core.schema.Test;
import com.variant.server.api.Session;

public class TestQualificationLifecycleEventImpl  implements TestQualificationLifecycleEvent {
	
	private Session session;
	private Test test;
	
	public TestQualificationLifecycleEventImpl(Session session, Test test) {
		this.session = session;
		this.test = test;
	}
	
	@Override
	public Test getTest() {
		return test;
	}
	
	@Override
	public UserHook<TestQualificationLifecycleEvent> getDefaultHook() {
		return new TestQualificationDefaultHook();
	}

	@Override
	public Session getSession() {
		return session;
	}

}
