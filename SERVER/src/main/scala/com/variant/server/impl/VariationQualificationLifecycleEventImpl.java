package com.variant.server.impl;

import com.variant.share.schema.Variation;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class VariationQualificationLifecycleEventImpl  implements VariationQualificationLifecycleEvent {
	
	private Session session;
	private Variation variation;
	
	public VariationQualificationLifecycleEventImpl(Session session, Variation variation) {
		this.session = session;
		this.variation = variation;
	}
	
	@Override
	public Variation getVariation() {
		return variation;
	}
	
	@Override
	public LifecycleHook<VariationQualificationLifecycleEvent> getDefaultHook() {
		return new VariationQualificationDefaultHook();
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public VariationQualificationLifecycleEvent.PostResult mkPostResult() {
		return new VariationQualificationLifecycleEventPostResultImpl(this);
	}

}
