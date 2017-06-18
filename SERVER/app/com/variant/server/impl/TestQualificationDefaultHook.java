package com.variant.server.impl;
/*
import java.util.Random;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.server.api.TestQualificationLifecycleEvent;

class TestQualificationDefaultHook implements UserHook<TestQualificationLifecycleEvent> {

	private static Random rand = new Random(System.currentTimeMillis());
	
	@Override
	public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
	}

	@Override
	public Hook getSchemaHook() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(Config arg0) throws Exception {}

	/**
	 * Default test qualifier. Qualify all.
	 * 
	 * @param session
	 * @param test
	 * @param state
	 * 
	 *
	@Override
	public UserHook.PostResult post(TestQualificationLifecycleEvent event) throws Exception {
		
		TestQualificationLifestyleEvent. resp = chain.mkResponse(TestQualificationLifecycleEvent.class);				
		resp.setQualified(true);
		
	}

	@Override
	public void post(Chain<TestQualificationLifecycleEvent> chain) throws Exception {
		
		if (!(chain instanceof TestQualificationLifecycleEvent.Chain)) {
			throw new RuntimeException("Wrong argument type");
		}
		
		
		
	}

}
*/