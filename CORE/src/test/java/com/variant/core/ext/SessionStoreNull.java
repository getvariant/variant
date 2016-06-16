package com.variant.core.ext;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantException;
import com.variant.core.impl.VariantCoreInitParamsImpl;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;

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

	private VariantCore coreApi = null;
	
	public SessionStoreNull() { }
	
	@Override
	public void initialized(VariantCoreInitParams initParams) {
		coreApi = ((VariantCoreInitParamsImpl)initParams).getCoreApi();
	}

	/**
	 * 
	 */
	@Override
	public VariantCoreSession get(String sessionId, Object...userData) {
		return new CoreSessionImpl(coreApi, sessionId);
	}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 * @throws VariantException 
	 */
	@Override
	public void save(VariantCoreSession session, Object...userData) throws VariantException {
		// don't save anything
	}
	
	@Override
	public void shutdown() {}

}
