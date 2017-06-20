package com.variant.server.impl;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.server.api.hook.PostResultFactory;
import com.variant.server.api.hook.TestQualificationLifecycleEvent;
import com.variant.server.api.hook.TestQualificationLifecycleEventPostResult;

class TestQualificationDefaultHook implements UserHook<TestQualificationLifecycleEvent> {
	
	/**
	 * Package visibility
	 */
	TestQualificationDefaultHook() {}
	
	@Override
	public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
	}

	@Override
	public void init(Config config, Hook hook) throws Exception {}

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
		
		TestQualificationLifecycleEventPostResult result = PostResultFactory.mkPostResult(event);				
		result.setQualified(true);
		return result;
	}

}
