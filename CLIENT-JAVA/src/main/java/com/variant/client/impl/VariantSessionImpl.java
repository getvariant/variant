package com.variant.client.impl;

import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.SessionScopedTargetingStabile;
import com.variant.core.impl.VariantCore;

/**
 * Client side session implementation. Adds client API to session state.
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl extends CoreSessionImpl implements VariantSession {

	private static final long serialVersionUID = 1L;
	
	private VariantClientImpl client;
	private VariantCore core;
	private VariantSessionIdTracker sessionIdTracker;
	private VariantTargetingTracker targetingTracker;
	
	private static SessionScopedTargetingStabile toTargetingStable(VariantTargetingTracker tt) {
	
		SessionScopedTargetingStabile result = new SessionScopedTargetingStabile();
		for (VariantTargetingTracker.Entry e: tt.get()) 
			result.add(e.getExperience(), e.getTimestamp());
		return result;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public VariantSessionImpl(VariantClient client, VariantSessionIdTracker sessionIdTracker, VariantTargetingTracker targetingTracker) {

		super(sessionIdTracker.get(), ((VariantClientImpl)client).getCoreApi(), toTargetingStable(targetingTracker));
		this.client = (VariantClientImpl) client;
		this.sessionIdTracker = sessionIdTracker;
		this.targetingTracker = targetingTracker;
	}

	

}
