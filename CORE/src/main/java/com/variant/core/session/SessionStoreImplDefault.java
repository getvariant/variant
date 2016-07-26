package com.variant.core.session;

import java.util.Map;

import com.variant.core.VariantSession;
import com.variant.core.impl.VariantCore;
import com.variant.core.net.SessionPayloadReader;

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
	public void save(VariantSession session) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void shutdown() {
		// Don't throw exception from lifecycle methods - we may never be called.		
	}
}
