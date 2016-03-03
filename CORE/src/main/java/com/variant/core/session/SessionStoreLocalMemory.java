package com.variant.core.session;

import java.util.HashMap;

import com.variant.core.InitializationParams;
import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantException;

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
	private Variant coreApi = null;
	
	SessionStoreLocalMemory() { }
	
	@Override
	public void initialized(InitializationParams initParams) {
		coreApi = initParams.getCoreApi();
	}

	/**
	 * 
	 */
	@Override
	public VariantSession get(String sessionId, Object...userData) {
		String json = map.get(sessionId);
		return json == null ? new VariantSessionImpl(coreApi, sessionId) : VariantSessionImpl.fromJson(coreApi, json);
	}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 * @throws VariantException 
	 */
	@Override
	public void save(VariantSession session, Object...userData) throws VariantException {
			map.put(session.getId(), ((VariantSessionImpl)session).toJson());
	}
	
	@Override
	public void shutdown() {
		map = null;
	}

}
