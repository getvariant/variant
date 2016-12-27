package com.variant.client;


import com.typesafe.config.Config;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.schema.Schema;

/**
 * Variant Bare Java Client. Makes no assumptions about the host application other than 
 * it is Java. 
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * Externally supplied configuration.
	 * See https://github.com/typesafehub/config for details on Typesafe Config.
	 * 
	 * @return An instance of the {@link Config} type.
	 * 
	 * @since 0.7
	 */
	public Config getConfig();
	
	/**
	 * Attempts to get a connection to the given server URL.
	 * 
	 * @param URL string of the form {@code <server-URI>:<schema-name>}. E.g. {@code https://localhost/variant:my-schema}
	 *        is a valid URL.
	 *        
	 * @return An instance of the {@link Connection} type.
	 * 
	 * @since 0.7
	 */
	public Connection getConnection(String url);
	

	/* ------------------------------------------------------------------------------------------------- */
	/* ------------------ NO CLIENT-SIDE HOOK MANAGEMENT.  REMOVE ME SOON ------------------------------
	 * <p>Register a {@link HookListener} with this client.
	 *
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link HookListener} interface.
	 *        
	 * @see HookListener
	 * @since 0.6
	 *
	public void addHookListener(HookListener<?> listener);
	
	
	/*
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 *
	public void clearHookListeners();
     *--------------------------------------------------------------------------------------------------*/
	
	/* ----------------------------- ON THE SERVER NOW ---------------------------------------------------
	 * Parse and, optionally, deploy an experiment schema.
	 * If no parse errors were encountered and the <code>deploy</code> argument is true, the schema 
	 * will be deployed
	 * 
	 * @param schema The experiment schema as an {@link java.io.InputStream}.
	 * @param deploy Weather or not to deploy the schema.
	 *        
	 * @return An instance of the {@link ParserResponse} type, which
	 *         may be examined for the information on the outcome of this operation.
	 * 
	 * @since 0.5
	 *
	public ParserResponse parseSchema(InputStream schema, boolean deploy);

	
	/**
	 * Parse and, if no errors, deploy an experiment schema.  
	 * Equivalent to <code>parseSchema(stream, true)</code>.
     * 
	 *         
	 * @param schema The experiment schema as an {@link java.io.InputStream}.
	 * @return An instance of the {@link ParserResponse} type, which
	 *         may be examined for the information on the outcome of this operation.
     *
	 * @since 0.5
	 *
	public ParserResponse parseSchema(InputStream schema);
    */
    
	/*
	 * Get currently deployed experiment schema, if any.
	 * 
	 * @return Current experiment schema as an instance of the {@link Schema} object.
	 * 
	 * @since 0.5
	 *
	public Schema getSchema();

	/**
	 * Get or create caller's current Variant session. If the session ID exists in the underlying implementation 
	 * of {@link VariantSessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned. Otherwise, a new session is created. If the session has not expired but the 
	 * schema has changed since it was created, this call will throw an unchecked 
	 * {@link VariantSchemaModifiedException}.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link VariantSessionIdTracker#init(VariantInitParams, Object...)}
	 *                 and {@link VariantTargetingTracker#init(VariantInitParams, Object...)}.
     *
	 * @since 0.6
	 * @return An object of type {@link VariantSession}. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object, unless the session expired between the
	 *         invocations, in which case a new object will be returned. Never returns <code>null</code>.
	 *
	public VariantSession getOrCreateSession(Object... userData);

	/**
	 * Get caller's current Variant session. If the session ID exists in the underlying implementation 
	 * of {@link VariantSessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned.  If the session has not expired but the schema has changed since it was created, 
	 * this call will throw an unchecked {@link VariantSchemaModifiedException}.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link VariantSessionIdTracker#init(Object...)}
	 *                 and {@link VariantTargetingTracker#init(VariantInitParams, Object...)}.
     *
	 * @since 0.6
	 * @return An object of type {@link VariantSession}. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object or <code>null</code>.
	 *
	public VariantSession getSession(Object... userData);
	
	/**
	 * Factory class: call <code>getInstance()</code> to obtain a new instance of {@link VariantClient}.
	 * 
	 * @since 0.6
	 */
	public static class Factory {
		
		/**
		 * Obtain a new instance of {@link VariantClient}.
		 * Host application should hold on to and reuse the object returned by this method whenever possible.
		 * One of these per address space is recommended.
		 * 
		 * @return Instance of the {@link VariantClient} type.
		 * @since 0.6
		 */
		public static VariantClient getInstance() {
			
			return new VariantClientImpl();
		}

	}
		
	
}
