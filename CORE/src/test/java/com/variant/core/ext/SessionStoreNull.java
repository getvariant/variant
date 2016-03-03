package com.variant.core.ext;

import com.variant.core.InitializationParams;
import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantException;
import com.variant.core.session.VariantSessionImpl;

/**
 * Session store that avoids any storage and always generates new session on get().
 * This saves time on JSON marshalling, for tests where we don't care.
 * 
 *** Good for tests only. ***
 * 
 * @author Igor
 *
 */
public class SessionStoreNull implements VariantSessionStore {

	private Variant coreApi = null;
	
	public SessionStoreNull() { }
	
	@Override
	public void initialized(InitializationParams initParams) {
		coreApi = initParams.getCoreApi();
	}

	/**
	 * 
	 */
	@Override
	public VariantSession get(String sessionId, Object...userData) {
		return new VariantSessionImpl(coreApi, sessionId);
	}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 * @throws VariantException 
	 */
	@Override
	public void save(VariantSession session, Object...userData) throws VariantException {
		// don't save anything
	}
	
	@Override
	public void shutdown() {}

}
