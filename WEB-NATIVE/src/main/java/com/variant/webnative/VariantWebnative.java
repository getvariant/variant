package com.variant.webnative;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.hook.HookListener;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantArrayUtils;
import com.variant.core.util.VariantStringUtils;
import com.variant.webnative.util.VariantWebStateRequestDecorator;

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
public class VariantWebnative {
	
	private Variant core;
	
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
	 * Obtain an instance of this API. Can be held on to and reused for the life of the JVM.
	 * 
	 * @param See {@link Variant.Factory#getInstance(String...)}
	 * @returns An instance of {@link VariantWebnative};
	 * @since 0.5
	 */
	public VariantWebnative(String...resourceNames) {
		this.core = Variant.Factory.getInstance(
				VariantArrayUtils.concat(
						resourceNames, 
						"/variant-webnative." + VariantStringUtils.RESOURCE_POSTFIX + ".props", 
						String.class));
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
	 * <p>Get user's Variant session. Typical code path likely will not need to call this method
	 * because the {@link #dispatchRequest(State, HttpServletRequest)} method will obtain
	 * the session internally.
	 * 
	 * @param httpRequest Current <code>HttpServletRequest</code>.
	 * @since 0.5
	 * @return
	 */
	public VariantSession getSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		return core.getSession(httpRequest, httpResponse);
	}
	
	/**
     * <p>Dispatch a new state request.
     *  
	 * @return An instance of the {@link com.variant.core.VariantStateRequest} object, which
	 *         may be further examined about the outcome of this operation. 
	 *
	 * @since 0.5
	 */
	public VariantStateRequest dispatchRequest(State state, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		
		VariantStateRequest result = core.dispatchRequest(getSession(httpRequest, httpResponse), state, httpRequest);
		return new VariantWebStateRequestDecorator(result, httpRequest);
	}
	
	/**
	 * Commit a state request. Flushes to storage this session's state.
     * 
	 * @param httpRequest Current <code>HttpServletRequest</code>.
	 * @param httpResponse Current <code>HttpServletResponse</code>.
     *
	 * @since 0.5
	 */
	public void commitStateRequest(VariantStateRequest request, HttpServletResponse httpResponse) {
		// The sessionStore may need either http response or http request, depending on the implementation.
		// We'll pass both.
		VariantWebStateRequestDecorator wrapper = (VariantWebStateRequestDecorator) request;
		core.commitStateRequest(wrapper.getOriginalRequest(), wrapper.getHttpServletRequest(), httpResponse);
	}
	
}
