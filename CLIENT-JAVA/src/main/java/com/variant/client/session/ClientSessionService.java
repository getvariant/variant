package com.variant.client.session;
/*
import static com.variant.client.VariantClientPropertyKeys.SESSION_ID_TRACKER_CLASS_INIT;
import static com.variant.client.VariantClientPropertyKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.VariantClientPropertyKeys.TARGETING_TRACKER_CLASS_INIT;
import static com.variant.client.VariantClientPropertyKeys.TARGETING_TRACKER_CLASS_NAME;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_SESSION_ID_TRACKER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_TARGETING_TRACKER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.RUN_SCHEMA_REPLACED;
import static com.variant.core.schema.impl.MessageTemplate.RUN_SCHEMA_UNDEFINED;
import static com.variant.core.schema.impl.MessageTemplate.RUN_SESSION_ID_NULL;

import java.util.Random;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantSession;
import com.variant.client.VariantSessionExpiredException;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.impl.VariantInitParamsImpl;
import com.variant.client.impl.VariantSessionImpl;
import com.variant.client.servlet.adapter.SessionIdTrackerHttpCookie.SsnIdCookie;
import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.session.CoreSessionService;
import com.variant.core.session.SessionStore;
import com.variant.core.util.VariantStringUtils;

public class ClientSessionService extends CoreSessionService {

	private static final Logger LOG  = LoggerFactory.getLogger(ClientSessionService.class);
	
	private VariantClientImpl client = null;

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 *
	public ClientSessionService(VariantClientImpl client) {
		super(client.getCoreApi());
		this.client = (VariantClientImpl) client;

	}
	
	/**
	 * Shutdown this SessionService.
	 * Cannot be used after this call.
	 *
	public void shutdown() {
		// kill me
	}
	
	/**
	 * Get or create а user session. If the session ID existed in the tracker and the corresponding
	 * VariantSession object has not yet expired,
	 * 
	 * @param userData opaque object(s) passed to the session ID tracker without inspection.
	 * @return 
	 *
	public VariantSession getSession(boolean create, Object...userData) {
		
		return null; // kill me
	}
		
}
*/