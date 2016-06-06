package com.variant.server;
/*
import java.util.Collection;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.event.impl.VariantEventDecoratorImpl;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.impl.VariantStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.VariantSessionImpl;
import com.variant.core.util.Tuples.Pair;

/**
 * Wraps malformed core session impl that only exposes methods
 * supported on the server.
 *
public class SessionServerWrapper implements VariantSession {

	private VariantSessionImpl coreImpl;
	
	public SessionServerWrapper(VariantSessionImpl coreImpl) {
		this.coreImpl = coreImpl;
	}

	@Override
	public long creationTimestamp() {
		return coreImpl.creationTimestamp();
	}

	@Override
	public String getId() {
		return coreImpl.getId();
	}

	@Override
	public String getSchemaId() {
		return coreImpl.getSchemaId();
	}

	@Override
	public VariantStateRequest getStateRequest() {
		return new StateRequestServerWrapper((VariantStateRequestImpl) coreImpl.getStateRequest());
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		throw new VariantInternalException("Method not supported");
	}
	
	@Override
	public Collection<Pair<Test, Boolean>> getTraversedTests() {
		throw new VariantInternalException("Method not supported");
	}

	/**
	 * Reimplement core impl in order to bind event to this
	 *
	@Override
	public void triggerEvent(VariantEvent event) {
		if (event == null) throw new IllegalArgumentException("Event cannot be null");		
		EventWriter ew = ((VariantCoreImpl) coreImpl.getCoreApi()).getEventWriter();
		ew.write(new VariantEventDecoratorImpl(event, this));
	}
}
*/