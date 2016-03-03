package com.variant.core;

import java.io.InputStream;

import com.variant.core.hook.HookListener;
import com.variant.core.hook.UserHook;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

/**
 * Variant Core API life cycle management. Top level methods. Call {@link Factory#getInstance()}
 * to obtain an instance.
 *
 * @author Igor Urisman.
 * @since 0.5
 */
public interface Variant {
		
	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link VariantProperties} type.
	 * 
	 * @since 0.6
	 */
	public VariantProperties getProperties();

	/**
	 * <p>Register a {@link com.variant.core.hook.HookListener}. The caller must provide 
	 * an implementation of the {@link com.variant.core.hook.HookListener} interface 
	 * which listens to a pre-defined {@link com.variant.core.hook.UserHook} type. Whenever 
	 * Variant reaches a hook given by the {@link com.variant.core.hook.UserHook} type
	 * parameter, the hook listener is notified. 
	 * 
	 * <p>Any number of listeners may listen for the same {@link com.variant.core.hook.UserHook} 
	 * type. If more than one listener is registered for a particular 
	 * {@link com.variant.core.hook.UserHook} type, the order of notification is undefined.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.core.hook.HookListener} interface.
	 *        
	 * @since 0.5
	 */
	public void addHookListener(HookListener<? extends UserHook> listener);
	

	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 */
	public void clearHookListeners();

	/**
	 * <p>Parse and, if no errors, optionally deploy a new experiment schema.
	 * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 * @param deploy The new test schema will be deployed if this is true and no parse errors 
	 *        were encountered.
	 *        
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} type that
	 *         may be further examined about the outcome of this operation.
	 * 
	 * @since 0.5
	 */
	public ParserResponse parseSchema(InputStream stream, boolean deploy);
			
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
	public ParserResponse parseSchema(InputStream stream);
	
	/**
	 * <p>Get currently deployed test schema, if any.
	 * 
	 * @return Current test schema as an instance of the {@link com.variant.core.schema.Schema} object.
	 * 
	 * @since 0.5
	 */
	public Schema getSchema();
		
	/**
	 * Get user's Variant session.
	 * 
	 * @param userData An array of 0 or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link com.variant.core.VariantSessionIdTracker#get(Object...)}
	 *                 and {@link com.variant.core.VariantSessionStore#get(Object...)}.
	 * @since 0.5
	 * @return
	 */
	public VariantSession getSession(Object...userData);
	
	/**
     * <p>Dispatch a new state request. See the Variant RCE User Guide for more information about Variant session
     * life cycle.
     *  
	 * @return An instance of the {@link com.variant.core.VariantStateRequest} object, which
	 *         may be further examined about the outcome of this operation. 
	 *
	 * @since 0.5
	 */
	public VariantStateRequest dispatchRequest(VariantSession session, State state, Object...targetingPersisterUserData);
	
	/**
	 * Commit a state request. Flushes to storage this session's state. See the Variant RCE User Guide for more information about Variant session
     * life cycle.
     * 
	 * @param request The state request to be committed.
	 * @param userData An array of 0 or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link com.variant.core.VariantSessionIdTracker#save(String, Object...)}
	 *                 and {@link com.variant.core.VariantSessionStore#save(VariantSession, Object...)}.
     *
	 * @since 0.5
	 */
	public void commitStateRequest(VariantStateRequest request, Object...userData);
		
	/**
	 * Factory singleton class for obtaining an instance of the Variant API.
	 * @since 0.5
	 */
	public static class Factory {
		
		/**
		 * <p>Obtain an instance of the Variant API. Takes 0 or more of String arguments. If supplied, 
		 * each argument is understood as a Java class path resource name. Each resource 
		 * is expected to contain a set of application properties, as specified by Java's 
		 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html#load-java.io.Reader-">Properties.load()</a> 
		 * method.
		 * 
		 * <p>When, at runtime, the container looks for the value of a particular property key, 
		 * these files are scanned left to right and the last value found takes precedence. 
		 * If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * the default value is used, as defiled in the <code>/variant-default.props</code> file 
		 * found inside the <code>variant-core-&lt;version&gt;.jar</code> file.
		 *
		 * @arg resourceNames 0 or more properties files as classpath resource names.
		 * @return An implementation of the {@link Variant} interface.
	     *
		 * @since 0.6
		 */
		public static Variant getInstance(String...resourceNames) {
			return new VariantCoreImpl(resourceNames);
		}
	}
}
