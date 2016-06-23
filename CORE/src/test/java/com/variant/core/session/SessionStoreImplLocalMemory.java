package com.variant.core.session;

import java.util.HashMap;
import java.util.Map;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantException;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.session.SessionStore;

/**
 * Session store implementation in local memory that uses JSON marshalling for better
 * approximation of the remote server.  In other words, the same session is gotten twice
 * will yield two different objects.
 * 
 * No external session ID tracking is assumed. Instead, session IDs
 * are passed in as user data. Sessions are stored in a map keyed by ID,
 * no expiration.
 * 
 *** Good for tests only. ***
 * 
 * @author Igor
 *
 */
public class SessionStoreImplLocalMemory implements SessionStore {

	private HashMap<String, String> map = new HashMap<String, String>();
	private VariantCore core = null;
	
	public SessionStoreImplLocalMemory() { }
	
	@Override
	public void init(VariantCore core, Map<String, Object> initObject) {
		this.core =  core;
	}

	/**
	 * 
	 */
	@Override
	public VariantCoreSession get(String sessionId) {
		String json = map.get(sessionId);
		return json == null ? null : CoreSessionImpl.fromJson(core, json);
	}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 * @throws VariantException 
	 */
	@Override
	public void save(VariantCoreSession session) throws VariantException {
			map.put(session.getId(), ((CoreSessionImpl)session).toJson());
	}
	
	@Override
	public void shutdown() {
		map = null;
	}

}
