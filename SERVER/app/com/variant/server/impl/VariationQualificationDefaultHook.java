package com.variant.server.impl;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

class VariationQualificationDefaultHook implements LifecycleHook<VariationQualificationLifecycleEvent> {
	
	/**
	 * Package visibility
	 */
	VariationQualificationDefaultHook() {}
	
	@Override
	public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
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
	public LifecycleHook.PostResult post(VariationQualificationLifecycleEvent event) throws Exception {
		
		VariationQualificationLifecycleEvent.PostResult result = event.newPostResult();				
		result.setQualified(true);
		return result;
	}

}
