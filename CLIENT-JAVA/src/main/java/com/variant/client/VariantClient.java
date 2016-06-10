package com.variant.client;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.client.impl.VariantClientSession;
import com.variant.core.VariantSession;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.CoreProperties;
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
public class VariantClient {
	
	private VariantCore core;
	
	// Cache most recently returned session for idempotency.
	VariantClientSession lastSession = null;
	
	//---------------------------------------------------------------------------------------------//
	//                                         PACKAGE                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * <p>Expose Core API to tests.
	 * 
	 * @return Core API Object.
	 * @since 0.5
	 */
	VariantCore getCoreApi() {
		return core;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * <p>Instantiate an instance of Variant client. Should be held on to and reused for the life of the JVM.
	 * Not thread safe: the host application should not use more than one of these at a time.
	 * 
	 * @since 0.6
	 */
	public VariantClient(String...resourceNames) {
		this.core = new VariantCore(
				VariantArrayUtils.concat(
						resourceNames,
						"/variant/client." + VariantStringUtils.RESOURCE_POSTFIX + ".props",
						String.class));
		((VariantCore)this.core).getComptime().registerComponent(VariantComptime.Component.CLIENT, "0.6.0");

	}

	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CoreProperties} type.
	 * 
	 * @since 0.6
	 */
	public CoreProperties getProperties() {
		return core.getProperties();
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
		core.addHookListener(listener);
	}
	
	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 */
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
	public VariantSession getSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		
		com.variant.core.VariantSession coreSession = core.getSession(httpRequest, httpResponse);
		
		VariantClientSession result = lastSession != null && lastSession.getCoreSession().equals(coreSession) ? 
				lastSession : new VariantClientSession(coreSession);
		
		lastSession = result;
		return result;
	}
			
}
