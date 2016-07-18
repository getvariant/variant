package com.variant.core.session;

import java.util.Map;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantException;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.session.SessionStore;

/**
 * Session store that avoids any storage and always generates new session on get().
 * This saves time on JSON marshalling, for tests where we don't care.
 * 
 *** Good for tests only. ***
 * 
 * @author Igor
 *
 */
public class SessionStoreImplNull implements SessionStore {

	private VariantCore core = null;
	
	public SessionStoreImplNull() { }
	
	@Override
	public void init(VariantCore core, Map<String, Object> initObject) {
		this.core = core;
	}

	/**
	 * 
	 */
	@Override
	public VariantCoreSession get(String sessionId, boolean create) {
		return new CoreSessionImpl(sessionId, core);
	}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 * @throws VariantException 
	 */
	@Override
	public void save(VariantCoreSession session) {
		// don't save anything
	}
	
	@Override
	public void shutdown() {}


}
