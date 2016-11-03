package com.variant.client;

import java.io.InputStream;
import java.util.Properties;

import com.variant.client.impl.VariantClientImpl;
import com.variant.core.exception.VariantSchemaModifiedException;
import com.variant.core.schema.Schema;
import com.variant.server.ParserResponse;
import com.variant.server.hook.HookListener;

/**
 * Variant Bare Java Client. Makes no assumptions about the host application other than 
 * it is Java. 
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * This client's application properties.
	 * These are the propertied that were in effect at the time when this Variant client instance was instantiated.
	 * 
	 * @return An instance of the {@link SystemProperties} type.
	 * 
	 * @since 0.6
	 */
	public SystemProperties getProperties();

	/**
	 * <p>Register a {@link HookListener} with this client.
	 *
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link HookListener} interface.
	 *        
	 * @see HookListener
	 * @since 0.6
	 */
	public void addHookListener(HookListener<?> listener);
	
	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 */
	public void clearHookListeners();

	/**
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
	 */
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
	 */
	public ParserResponse parseSchema(InputStream schema);

	/**
	 * Get currently deployed experiment schema, if any.
	 * 
	 * @return Current experiment schema as an instance of the {@link Schema} object.
	 * 
	 * @since 0.5
	 */
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
	 */
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
	 */
	public VariantSession getSession(Object... userData);
	
	/**
	 * Factory class: call <code>getInstance()</code> to obtain a new instance of {@link VariantClient}.
	 * 
	 * @since 0.6
	 */
	public static class Factory {
		
		/**
		 * Obtain a new instance of {@link VariantClient}. Takes zero or more String arguments
		 * which are understood to be Java classpath resource names. Each resource is expected 
		 * to contain a set of system property definitions, as specified by Java's 
		 * {@link Properties#load(java.io.Reader)} method. When Variant client needs to look 
		 * up a property value, these files are examined left to right and the first value found is
		 * used.  If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * a default is used.
		 * 
		 * Host application should hold on to and reuse the object returned by this method.
		 * 
		 * @param  resourceNames Zero or more system property files as classpath resource names.
		 * @return Instance of the {@link VariantClient} type.
		 * @since 0.6
		 */
		public static VariantClient getInstance(String...resourceNames) {
			
			return new VariantClientImpl(resourceNames);
		}

	}
		
	
}
