package com.variant.core.session;

import java.util.Map;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantException;
import com.variant.core.impl.VariantCore;

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
	public VariantCoreSession get(String sessionId) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void save(VariantCoreSession session) throws VariantException {
		throw new UnsupportedOperationException(MESSAGE);
	}
	
	@Override
	public void shutdown() {
		// Don't throw exception from lifecycle methods - we may never be called.		
	}

}
