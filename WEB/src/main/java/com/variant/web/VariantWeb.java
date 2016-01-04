package com.variant.web;

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
import com.variant.web.util.VariantWebStateRequestWrapper;

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
public class VariantWeb {
	
	private Variant core;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Obtain an instance of the API. Can be held on to and reused for the life of the JVM.
	 * The instance must be bootstrapped with a call to {@link #bootstrap(String...)} before it
	 * is usable.
	 * 
	 * @returns An instance of {@link VariantWeb};
	 * @since 0.5
	 */
	public VariantWeb() {
		this.core = Variant.Factory.getInstance();
	}
	
	/**
	 * Bootstrap the Variant container. Must be the first method called on a cold API
	 * after JVM startup.
	 * 
	 * @param resourceNames See {@link Variant#bootstrap(String...)}. 
	 * @since 0.5
	 */
	public void bootstrap(String...resourceNames) {
	
		String[] newArgs = new String[resourceNames.length + 1];
		for (int i = 0; i < resourceNames.length; i++) newArgs[i] = resourceNames[i];
		newArgs[resourceNames.length] = "/variant-web.props";
		core.bootstrap(newArgs);
	}
	
	/**
	 * Is the Variant container bootstrapped?
	 *
	 * @return true between calls to {@link #bootstrap} and {@link #shutdown}  methods, false otherwise.
	 * @since 0.5
	 */
	public boolean isBootstrapped() {
		return core.isBootstrapped();
	}
	
	/**
	 * <p>Shutdown Variant container. Releases all JVM resources associated with Variant Core API.
	 * Subsequently, calling any method other than {@link #bootstrap} will throw an exception.
	 * 
	 * @since 0.5
	 */
	public synchronized void shutdown() {
		core.shutdown();
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
	 * because the {@link #newStateRequest(State, HttpServletRequest)} method will obtain
	 * the session internally.
	 * 
	 * @param httpRequest Current <code>HttpServletRequest</code>.
	 * @since 0.5
	 * @return
	 */
	public VariantSession getSession(HttpServletRequest httpRequest) {
		return core.getSession(httpRequest);
	}
	
	/**
     * <p>Start a new state request.
     *  
	 * @return An instance of the {@link com.variant.core.VariantStateRequest} object, which
	 *         may be further examined about the outcome of this operation. 
	 *
	 * @since 0.5
	 */
	public VariantStateRequest newStateRequest(State state, HttpServletRequest httpRequest) {
		
		VariantStateRequest result = core.newStateRequest(getSession(httpRequest), state, httpRequest);
		return new VariantWebStateRequestWrapper(result, httpRequest);
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
		VariantWebStateRequestWrapper wrapper = (VariantWebStateRequestWrapper) request;
		core.commitStateRequest(wrapper.getOriginalRequest(), wrapper.getHttpServletRequest(), httpResponse);
	}

}
