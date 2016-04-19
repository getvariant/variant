package com.variant.core;

import java.io.InputStream;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.exception.VariantException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.UserHook;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
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
	 * Get user's Variant session. The contract of this method is that multiple calls with the same arguments
	 * will return the same object, provided the session did not expire between calls.  It is an error to
	 * call this method on an idle instance, i.e. before a valid schema has been parsed. 
	 * 
	 * @param userData An array of 0 or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link com.variant.core.VariantSessionIdTracker#get(Object...)}
	 *                 and {@link com.variant.core.VariantSessionStore#get(Object...)}.
	 * @since 0.5
	 * @return An instance of {@link VariantSession}.
	 */
	public VariantSession getSession(Object...userData);
				
	/**
	 * Factory singleton class for obtaining an instance of the Variant API.
	 * @since 0.5
	 */
	public static class Factory {
		
		private static final Logger LOG = LoggerFactory.getLogger(Variant.class);
		
		/**
		 * <p>Obtain an instance of the Variant API. Takes 0 or more of String arguments. If supplied, 
		 * each argument is understood as a Java class path resource name. Each resource 
		 * is expected to contain a set of application properties, as specified by Java's 
		 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html#load-java.io.Reader-">Properties.load()</a> 
		 * method.
		 * 
		 * <p>When, at runtime, the container looks for the value of a particular property key, 
		 * these files are scanned left to right and the first value found is returned. 
		 * If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * the default value is used, as defiled in the <code>/variant/defaults.props</code> file 
		 * found inside the <code>variant-core-&lt;version&gt;.jar</code> file.
		 *
		 * @arg resourceNames 0 or more properties files as classpath resource names.
		 * @return An implementation of the {@link Variant} interface.
	     *
		 * @since 0.6
		 */
		public static Variant getInstance(String...resourceNames) {
			
			long now = System.currentTimeMillis();
			VariantCoreImpl result;
			try {
				result = new VariantCoreImpl(resourceNames);
			}
			catch (final VariantException e) {
				throw e;
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to instantiate Core", e);
			}
			LOG.info(
					String.format("Variant Core %s bootstrapped in %s.",
							result.getComptime().getCoreVersion(),
							DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
			
			return result;
		}
		
	}
}
