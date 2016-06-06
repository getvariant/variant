package com.variant.client;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantArrayUtils;
import com.variant.core.util.VariantStringUtils;

/**
 * <p>Variant Web API. The platform API suitable for Web applications written on top of the Java Servlet API. 
 * It is a facade around the {@link com.variant.core.Variant Core API} with the advantage of hiding much 
 * of its complexities behind a new set of methods that operate on familiar Servlet API objects.
 * To obtain an instance, instantiate with the constructor:
 * 
 * <p>
 * <code>VariantWeb webAPI = new {@link #VariantWeb()};</code>
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class VariantClient {
	
	private Variant core;
	
	// Cache most recently returned session for idempotency.
	VariantSession lastSession = null;
	
	//---------------------------------------------------------------------------------------------//
	//                                         PACKAGE                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Expose Core API to tests.
	 * @return
	 */
	Variant getCoreApi() {
		return core;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Obtain an instance of Variant client. Can be held on to and reused for the life of the JVM.
	 * 
	 * @param See {@link Variant.Factory#getInstance(String...)}
	 * @returns An instance of {@link VariantClient};
	 * @since 0.5
	 */
	public VariantClient(String...resourceNames) {
		this.core = Variant.Factory.getInstance(
				VariantArrayUtils.concat(
						resourceNames,
						"/variant/client." + VariantStringUtils.RESOURCE_POSTFIX + ".props",
						String.class));
		((VariantCoreImpl)this.core).getComptime().registerComponent(VariantComptime.Component.CLIENT, "0.6.0");

	}

	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link VariantProperties} type.
	 * 
	 * @since 0.6
	 */
	public VariantProperties getProperties() {
		return core.getProperties();
	}

	/**
	 * <p>Register a {@link com.variant.core.hook.HookListener}. 
	 * See {@link Variant#addHookListener(HookListener)} for details.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.core.hook.HookListener} interface.
	 *        
	 * @since 0.5
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
		
		VariantSession result = lastSession != null && lastSession.getCoreSession().equals(coreSession) ? 
				lastSession : new VariantSession(coreSession);
		
		lastSession = result;
		return result;
	}
			
}
