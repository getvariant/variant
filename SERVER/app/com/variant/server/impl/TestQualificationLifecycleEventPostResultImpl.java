package com.variant.server.impl;

import com.variant.server.api.hook.TestQualificationLifecycleEvent;
import com.variant.server.api.hook.TestQualificationLifecycleEventPostResult;

public class TestQualificationLifecycleEventPostResultImpl implements TestQualificationLifecycleEventPostResult {

	private boolean qualified = false;
	private boolean removeFromTT = false;
	
	public TestQualificationLifecycleEventPostResultImpl(TestQualificationLifecycleEvent event) {}
	
	@Override
	public void setQualified(boolean qualified) {
		this.qualified = qualified;
	}

	@Override
	public void setRemoveFromTargetingTracker(boolean removeFromTT) {
		this.removeFromTT = removeFromTT;
	}

	public boolean isQualified() { 
		return qualified; 
	}
	
	public boolean isRemoveFromTT() { 
		return removeFromTT; 
	}

}

