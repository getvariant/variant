package com.variant.client.servlet.adapter;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.VariantClient;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.CorePropertiesImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.parser.ParserResponse;

/**
 * <p>Servlet-aware wrapper around {@link VariantClient} that replaces all environment contingent
 * method signatures with ones operating on Servlet API types. Host applications should use this
 * interface if they are Web applicaitons running on top of the Servlet API.
 * 
 * @author Igor Urisman
 * 
 * @see {@link VariantClient}.
 * 
 * @since 0.6
 */
public class VariantServletClient {

	private VariantClient client;
	
	/**
	 * <p>Instantiate an instance of Variant client Servlet adapter. Takes 0 or more of String arguments. 
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
	public VariantServletClient(String...resourceNames) {	
		this.client = VariantClient.Factory.getInstance(resourceNames);
	}

	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CorePropertiesImpl} type.
	 * 
	 * @since 0.6
	 */
	public VariantProperties getProperties() {
		return client.getProperties();
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
	public void addHookListener(HookListener<?> listener) {
		client.addHookListener(listener);
	}
	
	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.6
	 */
	public void clearHookListeners() {
		client.clearHookListeners();
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
	 * @since 0.6
	 */
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {
		return client.parseSchema(stream, deploy);
	}

	/**
	 * <p>Parse and, if no errors, deploy a new experiment schema.  Same as 
     * <code>parseSchema(stream, true)</code>.
     * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 *         
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} type, which
	 *         may be further examined about the outcome of this operation.
     *
	 * @since 0.6
	 */
	public ParserResponse parseSchema(InputStream stream) {
		return client.parseSchema(stream);
	}

	/**
	 * <p>Get currently deployed test schema, if any.
	 * 
	 * @return Current test schema as an instance of the {@link com.variant.core.schema.Schema} object.
	 * 
	 * @since 0.6
	 */
	public Schema getSchema() {
		return client.getSchema();
	}

	/**
	 * <p>Get user's Variant session.
	 * 
	 * @param userData
	 * @since 0.6
	 * @return
	 */
	public VariantSession getSession(HttpServletRequest request) {
		return client.getSession(request);
	}

}
