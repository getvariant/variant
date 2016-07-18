package com.variant.core.session;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.inject.Injector;

/**
 * Core session service.
 * 
 * @author Igor
 *
 */
public class CoreSessionService {
	
	VariantCore core;
	SessionStore sessionStore;
	
	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public CoreSessionService(VariantCore core) {
		this.core = (VariantCore) core;
		this.sessionStore = Injector.inject(SessionStore.class, core);
	}
	
	/**
	 * Shutdown this service. Cannot be used after this call.
	 */
	public void shutdown() {
		sessionStore.shutdown();
		sessionStore = null;
	}
	
	/**
	 * Get a session from the underlying session store. 
	 * Recreate if does not exist and <code>create</code> is true.
	 * @return 
	 */
	public VariantCoreSession getSession(String id, boolean create) throws VariantRuntimeException {
		if (core.getSchema() == null) throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_SCHEMA_UNDEFINED);
		return  (CoreSessionImpl) sessionStore.get(id, create);
	}
	
	/**
	 * Persist user session in session store.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(VariantCoreSession session) {
		
		if (core.getSchema() == null) throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_SCHEMA_UNDEFINED);
		
		if (!core.getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_SCHEMA_MODIFIED, core.getSchema().getId(), session.getSchemaId());
		
		sessionStore.save(session);
	}

}
