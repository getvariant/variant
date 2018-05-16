package com.variant.server.impl;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.lifecycle.TestQualificationLifecycleEvent;
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
	public LifecycleHook<TestQualificationLifecycleEvent> getDefaultHook() {
		return new TestQualificationDefaultHook();
	}

	@Override
	public Session getSession() {
		return session;
	}

}
