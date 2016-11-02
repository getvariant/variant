package com.variant.core.test;

import java.util.HashMap;
import java.util.Map;

import com.variant.client.session.SessionStore;
import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantException;
import com.variant.core.impl.SessionId;
import com.variant.core.impl.VariantCore;
import com.variant.core.net.Payload;
import com.variant.core.net.PayloadWriter;
import com.variant.core.net.SessionPayloadReader;
import com.variant.core.session.CoreSession;

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
	private int sessionTimeoutSeconds;
	private String serverRelease;
	
	public SessionStoreImplLocalMemory() { }
	
	@Override
	public void init(VariantCore core, Map<String, Object> initObject) {
		this.core =  core;
		serverRelease = (String)initObject.get("svr_rel");
		sessionTimeoutSeconds = (Integer)initObject.get("ssn_timeout_sec");
	}

	/**
	 * 
	 */
	@Override
	public SessionPayloadReader get(String sessionId, boolean create) {
		String jsonBody = map.get(sessionId);
		if (jsonBody == null && create) {
			jsonBody = new CoreSession(new SessionId(sessionId), core).toJson();
			map.put(sessionId, jsonBody);
		}  
		
		if (jsonBody == null) return new SessionPayloadReader(core, null);
		
		PayloadWriter pw = new PayloadWriter(jsonBody);
		pw.setProperty(Payload.Property.SVR_REL, serverRelease);
		pw.setProperty(Payload.Property.SSN_TIMEOUT, String.valueOf(sessionTimeoutSeconds));
		
		return new SessionPayloadReader(core, pw.getAsJson());
	}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 * @throws VariantException 
	 */
	@Override
	public void save(VariantCoreSession session) {
			//System.out.println(((CoreSessionImpl)session).toJson());
			map.put(session.getId(), ((CoreSession)session).toJson());
	}
	
	@Override
	public void shutdown() {
		map = null;
	}

}
