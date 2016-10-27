package com.variant.core.session;

import java.util.Map;

import com.variant.core.VariantCoreSession;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.SessionId;
import com.variant.core.impl.VariantCore;
import com.variant.core.net.PayloadWriter;
import com.variant.core.net.SessionPayloadReader;

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
	
	@Override
	public void shutdown() {}

	@Override
	public SessionPayloadReader get(String sessionId, boolean create) {
		
		String payload = new PayloadWriter(new CoreSessionImpl(new SessionId(sessionId), core).toJson()).getAsJson();
		return new SessionPayloadReader(core, payload);
	}

	@Override
	public void save(VariantCoreSession session) {
		// No-op.		
	}


}
