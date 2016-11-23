package com.variant.core.session;


/**
 * Session store that avoids any storage and always generates new session on get().
 * This saves time on JSON marshalling, for tests where we don't care.
 * 
 *** Good for tests only. ***
 * 
 * @author Igor
 *
 *
public class SessionStoreImplNull implements SessionStore {
	
	public SessionStoreImplNull() { }
	
	@Override
	public void init(Map<String, Object> initObject) {
	}
	
	@Override
	public void shutdown() {}

	@Override
	public SessionPayloadReader get(String sessionId, boolean create) {
		
		String payload = new PayloadWriter(new CoreSession(new SessionId(sessionId), core).toJson()).getAsJson();
		return new SessionPayloadReader(payload);
	}

	@Override
	public void save(CoreSession session) {
		// No-op.		
	}


}
*/