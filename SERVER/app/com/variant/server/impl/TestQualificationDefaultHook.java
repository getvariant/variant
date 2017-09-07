package com.variant.server.impl;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.server.lce.TestQualificationLifecycleEvent;
import com.variant.core.schema.Hook;
import com.variant.server.api.PostResultFactory;

class TestQualificationDefaultHook implements UserHook<TestQualificationLifecycleEvent> {
	
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
	public UserHook.PostResult post(TestQualificationLifecycleEvent event) throws Exception {
		
		TestQualificationLifecycleEvent.PostResult result = PostResultFactory.mkPostResult(event);				
		result.setQualified(true);
		return result;
	}

}
