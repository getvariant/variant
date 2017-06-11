package com.variant.server.impl;

import com.variant.core.schema.Test;
import com.variant.server.api.Session;
import com.variant.server.api.StateRequest;
import com.variant.server.api.TestQualificationLifecycleEvent;

public class TestQualificationLifecycleEventImpl  implements TestQualificationLifecycleEvent {
	
	private Session session;
	private Test test;
	private boolean qualified = true;
	private boolean removeFromTT = false;
	
	public TestQualificationLifecycleEventImpl(Session session, Test test) {
		this.session = session;
		this.test = test;
	}
	
	@Override
	public Test getTest() {
		return test;
	}

	@Override
	public boolean isQualified() {
		return qualified;
	}
	
	@Override
	public boolean isRemoveFromTargetingTracker() {
		return removeFromTT;
	}
	
	@Override
	public StateRequest getStateRequest() {
		return session.getStateRequest();
	}

	@Override
	public void setQualified(boolean qualified) {
		this.qualified = qualified;
	}

	@Override
	public void setRemoveFromTargetingTracker(boolean remove) {
		removeFromTT = remove;
	}

}
