package com.variant.core.ext;

import java.util.HashMap;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantException;
import com.variant.core.impl.VariantCoreInitParamsImpl;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;

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
public class SessionStoreLocalMemory implements VariantSessionStore {

	private HashMap<String, String> map = new HashMap<String, String>();
	private VariantCore coreApi = null;
	
	public SessionStoreLocalMemory() { }
	
	@Override
	public void initialized(VariantCoreInitParams initParams) {
		coreApi =  ((VariantCoreInitParamsImpl)initParams).getCoreApi();
	}

	/**
	 * 
	 */
	@Override
	public VariantCoreSession get(String sessionId) {
		String json = map.get(sessionId);
		return json == null ? new CoreSessionImpl(coreApi, sessionId) : CoreSessionImpl.fromJson(coreApi, json);
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
