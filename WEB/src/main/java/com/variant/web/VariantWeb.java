package com.variant.web;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;


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
	public static void bootstrap() {
		
		engine.bootstrap("/variant-web.props");
	}
	
	/**
	 * Programmatically shutdown Variant engine.
	 */
	public static synchronized void shutdown() {
		engine.shutdown();
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
	public static VariantSession getSession(boolean create, HttpServletRequest request) {
		VariantSession result = engine.getSession(create, request);
		result.initTargetingPersister(request);
		return result;
	}
	
	/**
	 * Get user's Variant session. 
	 * 
	 * @param request Active <code>HttpServletRequest</code> object.
	 * @return
	 */
	public static VariantSession getSession(HttpServletRequest request) {
		return getSession(true, request);
	}

	/**
     * Start view Request 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public static VariantViewRequest startViewRequest(VariantSession session, View view) {
		return engine.startViewRequest(session, view);
	}
	
	/**
	 * Commit a view request.
	 * @param request
	 */
	public static void commitViewRequest(VariantViewRequest viewRequest, HttpServletResponse httpResponse) {
		engine.commitViewRequest(viewRequest, httpResponse);
	}

}
