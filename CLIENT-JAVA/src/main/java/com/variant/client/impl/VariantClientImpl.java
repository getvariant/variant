package com.variant.client.impl;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantClient;
import com.variant.client.VariantClientPropertyKeys;

import static com.variant.client.VariantClientPropertyKeys.Key;

import com.variant.client.VariantSession;
import com.variant.client.session.ClientSessionService;
import com.variant.core.VariantProperties;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantArrayUtils;
import com.variant.core.util.VariantStringUtils;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantClientImpl implements VariantClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(VariantClientImpl.class);
			
	private VariantCore core = null;
	private VariantProperties properties = null;
	private ClientSessionService sessionService = null;
	
	//---------------------------------------------------------------------------------------------//
	//                                         PACKAGE                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	ClientSessionService getSessionService() {
		return sessionService;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl(String...resourceNames) {
		
		core = new VariantCore(resourceNames);

		core.getComptime().registerComponent(VariantComptime.Component.CLIENT, "0.6.1");
		
		sessionService = new ClientSessionService(this);
		properties = core.getProperties();


		if (LOG.isDebugEnabled()) {
			LOG.debug("+-- Bootstrapping Variant Client with following application properties: --");
			for (Key key: Key.keys(VariantClientPropertyKeys.class)) {
				LOG.debug("| " + key.propertyName() + " = " + properties.get(key, String.class) + " : " + properties.getSource(key));
			}
			LOG.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}
	}

	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CoreProperties} type.
	 * 
	 * @since 0.6
	 */
	@Override
	public VariantProperties getProperties() {
		return properties;
	}

	/**
	 * <p>Register a {@link HookListener}.
	 * See {@link Variant#addHookListener(HookListener)} for details.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.core.hook.HookListener} interface.
	 *        
	 * @since 0.6
	 */
	@Override
	public void addHookListener(HookListener<?> listener) {
		core.addHookListener(listener);
	}
	
	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 */
	@Override
	public void clearHookListeners() {
		core.clearHookListeners();
	}

	/**
	 * <p>Parse and, if no errors, optionally deploy a new experiment schema.
	 * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 * @param deploy The new test schema will be deployed if this is true and no parse errors 
	 *        were encountered.
	 *        
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} object that
	 *         may be further examined about the outcome of this operation.
	 * 
	 * @since 0.5
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {		
		return core.parseSchema(stream, deploy);
	}

	/**
	 * <p>Parse and, if no errors, deploy a new experiment schema.  Same as 
     * <code>parseSchema(stream, true)</code>.
     * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 *         
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} object, which
	 *         may be further examined about the outcome of this operation.
     *
	 * @since 0.5
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream) {
		return core.parseSchema(stream);
	}

	/**
	 * <p>Get currently deployed test schema, if any.
	 * 
	 * @return Current test schema as an instance of the {@link com.variant.core.schema.Schema} object.
	 * 
	 * @since 0.5
	 */
	@Override
	public Schema getSchema() {
		return core.getSchema();
	}

	/**
	 * <p>Get user's Variant session.
	 * 
	 * @param httpRequest Current <code>HttpServletRequest</code>.
	 * @since 0.5
	 * @return
	 */
	@Override
	public VariantSession getSession(Object... userData) {
		return sessionService.getSession(userData);
	}
			
	//---------------------------------------------------------------------------------------------//
	//                                      PUBLIC EXT                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * @return Core API Object.
	 * @since 0.5
	 */
	public VariantCore getCoreApi() {
		return core;
	}

}
