package com.variant.core.session;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantRuntimeException;
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
	public CoreSessionService(VariantCore core) throws VariantBootstrapException {
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
	 * Get or create user session.
	 * @return 
	 */
	public VariantCoreSession getSession(String id) throws VariantRuntimeException {
		
		if (core.getSchema() == null) throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED);

		// Get the session by ID from the session store.  NULL if desn't exist or expired.
		CoreSessionImpl result =  (CoreSessionImpl) sessionStore.get(id);

		// If none, create new and save in store.
		if (result == null) {
			result = new CoreSessionImpl(id, core);
			sessionStore.save(result);
		}
		
		return result;	
	}
	
	/**
	 * Persist user session in session store.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(CoreSessionImpl session) {
		
		if (core.getSchema() == null) throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED);
		
		if (!core.getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_REPLACED, core.getSchema().getId(), session.getSchemaId());
		
		sessionStore.save(session);
	}

}
