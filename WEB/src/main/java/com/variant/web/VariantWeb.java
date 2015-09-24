package com.variant.web;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
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

	private static Variant engine = Variant.Factory.getInstance();
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Bootstrap the Variant engine.
	 * 
	 * @throws VariantBootstrapException
	 */
	public static void bootstrap(String...resourceNames) {
	
		String[] newArgs = new String[resourceNames.length + 1];
		for (int i = 0; i < resourceNames.length; i++) newArgs[i] = resourceNames[i];
		newArgs[resourceNames.length] = "/variant-web.props";
		engine.bootstrap(newArgs);
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isBootstrapped() {
		return engine.isBootstrapped();
	}
	
	/**
	 * Programmatically shutdown Variant engine.
	 */
	public static synchronized void shutdown() {
		engine.shutdown();
	}
	
	/**
	 * Client code may extend default semantics by supplying flashpoint listeners.
	 * 
	 * @param listener
	 */
	public static void addFlashpointListener(FlashpointListener<?> listener) {
		engine.addFlashpointListener(listener);
	}
	

	/**
	 * Remove all current flashpoint listeners.
	 * 
	 * @param listener
	 */
	public static void clearFlashpointListeners() {
		engine.clearFlashpointListeners();
	}

	/**
	 * Parse from an input stream, and, if no errors, optionally deploy a new schema.
	 * @param stream
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public static ParserResponse parseSchema(InputStream stream, boolean deploy) {		
		return engine.parseSchema(stream, deploy);
	}

	/**
	 * Parse from an input stream, and, if no errors, deploy a new schema.
	 * @param stream
	 * @return
	 */
	public static ParserResponse parseSchema(InputStream stream) {
		return engine.parseSchema(stream);
	}

	/**
	 * Parse from a string, and, if no errors, optionally deploy a new schema.
	 * @param string
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public static ParserResponse parseSchema(String string, boolean deploy) {
		return engine.parseSchema(string, deploy);
	}

	/**
	 * Parse from a string, and, if no errors, deploy a new schema.
	 * @param string
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public static ParserResponse parseSchema(String string) {
		return engine.parseSchema(string);
	}

	/**
	 * Get current test schema.
	 * @return Current test schema or null, if none has been deployed yet.
	 */
	public static Schema getSchema() {
		return engine.getSchema();
	
	}

	/**
	 *  Get user's Variant session. 
	 *  
	 * @param create  Whether or not create the session if does not exist.
	 * @param request Active <code>HttpServletRequest</code> object.
	 * @return          
	 */
	public static VariantSession getSession(HttpServletRequest request) {
		return engine.getSession(request);
	}
	
	/**
     * Start view Request 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public static VariantStateRequest newStateRequest(State view, HttpServletRequest request) {
		return engine.newStateRequest(getSession(request), view, request);
	}
	
	/**
	 * Commit a view request.
	 * @param request
	 */
	public static void commitViewRequest(VariantStateRequest viewRequest, HttpServletResponse httpResponse) {
		engine.commitStateRequest(viewRequest, httpResponse);
	}

}
