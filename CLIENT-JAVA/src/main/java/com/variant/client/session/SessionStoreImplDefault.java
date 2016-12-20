package com.variant.client.session;

import java.util.Map;

import com.variant.core.impl.VariantCore;
import com.variant.core.net.SessionPayloadReader;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionStore;

/**
 * Default session store implementation.  All methods throw {@link UnsupportedOperationException}.
 * 
 * @author Igor
 *
 */
public class SessionStoreImplDefault implements SessionStore {
	
	private static final String MESSAGE = "Inject a functional implementation of session store";
	
	@Override
	public void init(VariantCore core, Map<String, Object> initObject) {
		// Don't throw exception from lifecycle methods - we may never be called.
	}
	
	@Override
	public SessionPayloadReader get(String sessionId, boolean create) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void save(CoreSession session) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void shutdown() {
		// Don't throw exception from lifecycle methods - we may never be called.		
	}
}
