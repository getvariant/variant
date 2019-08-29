package com.variant.server.impl;

import java.util.Optional;

import com.variant.server.api.lifecycle.LifecycleHook;
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
	public Optional<VariationQualificationLifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) throws Exception {
		
		VariationQualificationLifecycleEvent.PostResult result = event.mkPostResult();				
		result.setQualified(true);
		return Optional.of(result);
	}

}
