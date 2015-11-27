package com.variant.web;

/**
 * Java Servlet Domain API.
 */
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;


/**
 * Variant Web API
 * 
 * @author Igor
 *
 */
public class VariantWeb {
	
	private Variant core;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public VariantWeb() {
		this.core = Variant.Factory.getInstance();
	}
	
	/**
	 * Bootstrap the Variant engine.
	 * 
	 * @throws VariantBootstrapException
	 */
	public void bootstrap(String...resourceNames) {
	
		String[] newArgs = new String[resourceNames.length + 1];
		for (int i = 0; i < resourceNames.length; i++) newArgs[i] = resourceNames[i];
		newArgs[resourceNames.length] = "/variant-web.props";
		core.bootstrap(newArgs);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isBootstrapped() {
		return core.isBootstrapped();
	}
	
	/**
	 * Programmatically shutdown Variant engine.
	 */
	public synchronized void shutdown() {
		core.shutdown();
	}
	
	/**
	 * Client code may extend default semantics by supplying flashpoint listeners.
	 * 
	 * @param listener
	 */
	public void addFlashpointListener(FlashpointListener<?> listener) {
		core.addFlashpointListener(listener);
	}
	

	/**
	 * Remove all current flashpoint listeners.
	 * 
	 * @param listener
	 */
	public void clearFlashpointListeners() {
		core.clearFlashpointListeners();
	}

	/**
	 * Parse from an input stream, and, if no errors, optionally deploy a new schema.
	 * @param stream
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {		
		return core.parseSchema(stream, deploy);
	}

	/**
	 * Parse from an input stream, and, if no errors, deploy a new schema.
	 * @param stream
	 * @return
	 */
	public ParserResponse parseSchema(InputStream stream) {
		return core.parseSchema(stream);
	}

	/**
	 * Get current test schema.
	 * @return Current test schema or null, if none has been deployed yet.
	 */
	public Schema getSchema() {
		return core.getSchema();
	
	}

	/**
	 *  Get user's Variant session. 
	 *  
	 * @param request Active <code>HttpServletRequest</code> object.
	 * @return          
	 */
	public VariantSession getSession(HttpServletRequest httpRequest) {
		return core.getSession(httpRequest);
	}
	
	/**
     * Start a new state request 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public VariantStateRequest newStateRequest(State state, HttpServletRequest httpRequest) {
		
		VariantStateRequest result = core.newStateRequest(getSession(httpRequest), state, httpRequest);
		return new VariantWebStateRequestWrapper(result, httpRequest);
	}
	
	/**
	 * Commit a state request.
	 * @param request
	 */
	public void commitStateRequest(VariantStateRequest request, HttpServletResponse httpResponse) {
		// The sessionStore may need either http response or http request, depending on the implementation.
		// We'll pass both.
		VariantWebStateRequestWrapper wrapper = (VariantWebStateRequestWrapper) request;
		core.commitStateRequest(wrapper.getOriginalRequest(), wrapper.getHttpServletRequest(), httpResponse);
	}

}
