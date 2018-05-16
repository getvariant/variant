package com.variant.server.impl;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.PostResultFactory;
import com.variant.server.lifecycle.TestQualificationLifecycleEvent;

class TestQualificationDefaultHook implements LifecycleHook<TestQualificationLifecycleEvent> {
	
	/**
	 * Package visibility
	 */
	TestQualificationDefaultHook() {}
	
	@Override
	public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
	}

	/**
	 * Default test qualifier. Qualify all.
	 * 
	 * @param session
	 * @param test
	 * @param state
	 * 
	 */
	@Override
	public LifecycleHook.PostResult post(TestQualificationLifecycleEvent event) throws Exception {
		
		TestQualificationLifecycleEvent.PostResult result = PostResultFactory.mkPostResult(event);				
		result.setQualified(true);
		return result;
	}

}
