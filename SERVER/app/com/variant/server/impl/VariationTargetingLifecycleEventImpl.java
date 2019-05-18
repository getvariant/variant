package com.variant.server.impl;

import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;

/**
 * 
 */
public class VariationTargetingLifecycleEventImpl implements VariationTargetingLifecycleEvent {

	private Session session;
	private Variation variation;
	private State state;
	
	public VariationTargetingLifecycleEventImpl(Session session, Variation variation, State state) {
		this.session = session;
		this.variation = variation;
		this.state = state;
	}
	
	@Override
	public Variation getVariation() {
		return variation;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public LifecycleHook<VariationTargetingLifecycleEvent> getDefaultHook() {
		
		return new VariationTargetingDefaultHook();
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public VariationTargetingLifecycleEvent.PostResult newPostResult() {
		return new VariationTargetingLifecycleEventPostResultImpl(this);
	}
	

}		
