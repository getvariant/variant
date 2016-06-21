package com.variant.client;

import java.io.InputStream;

import com.variant.client.impl.VariantClientImpl;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.CorePropertiesImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.parser.ParserResponse;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantClient {
		
	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CorePropertiesImpl} type.
	 * 
	 * @since 0.6
	 */
	public VariantProperties getProperties();

	/**
	 * <p>Register a {@link HookListener}.
	 * See {@link Variant#addHookListener(HookListener)} for details.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.core.hook.HookListener} interface.
	 *        
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
	public ParserResponse parseSchema(InputStream stream, boolean deploy);

	/**
	 * <p>Parse and, if no errors, deploy a new experiment schema.  Same as 
     * <code>parseSchema(stream, true)</code>.
     * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 *         
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} type, which
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
	 * <p>Get user's Variant session.
	 * 
	 * @param userData
	 * @since 0.5
	 * @return
	 */
	public VariantSession getSession(Object... userData);
			
	
	/**
	 * <p>Factory class
	 * @author Igor Urisman
	 *
	 */
	public static class Factory {
		
		/**
		 * <p>Instantiate an instance of Variant client. Takes 0 or more of String arguments. 
		 * If supplied, each argument is understood as a Java class path resource name. Each 
		 * resource is expected to contain a set of application properties, as specified by 
		 * Java's Properties.load() method. When VariantClient needs to look up a property
		 * value, these files are scanned left to right and the first value found is used. 
		 * If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * a default is used.
		 * 
		 * <p>Host application should hold on to and reuse the object returned by this method.
		 * Not thread safe: the host application should not use more than one of these at a time.
		 * 
		 * @param  0 or more classpath resource names.
		 * @return Instance of the {@link VariantClient} type
		 * @since 0.6
		 */
		public static VariantClient getInstance(String...resourceNames) {
			
			return new VariantClientImpl(resourceNames);
		}

	}
		
	
}
