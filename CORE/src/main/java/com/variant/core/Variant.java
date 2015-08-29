package com.variant.core;

import java.io.InputStream;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.TestParsedEventListener;
import com.variant.core.schema.View;
import com.variant.core.schema.ViewParsedEventListener;

/**
 * The Variant CORE API.
 * 
 * @author Igor
 *
 */
public interface Variant {
	
	/**
	 * Engine bootstrap. Takes 0 or more number of properties files as resource names.
	 * If supplied, they are scanned at run time left to right until first match.
	 * Can be overridden at run time.
	 */
	public void bootstrap(String...resourceNames) throws VariantBootstrapException;	

	/**
	 * Programmatically shutdown Variant engine.
	 */
	public void shutdown();
	
	/**
	 * Clients may add additional parse semantics via parse listeners.
	 * Each time the core parser successfully completes parsing of a Test object,
	 * this listener will be invoked in the same thread.
	 * 
	 * @param listener
	 */
	public void addListener(TestParsedEventListener listener);
	
	/**
	 * Clients may add additional parse semantics via parse listeners.
	 * Each time the core parser successfully completes parsing of a View object,
	 * this listener will be invoked in the same thread.
	 * 
	 * @param listener
	 */
	public void addListener(ViewParsedEventListener listener);

	/**
	 * Parse and, if no errors, optionally deploy a new schema.
	 * @param stream
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public ParserResponse parseSchema(InputStream stream, boolean deploy);
		
	/**
	 * Parse and, if no errors, optionally deploy a new schema.
	 * @param string
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public ParserResponse parseSchema(String string, boolean deploy);
	
	/**
	 * Parse and, if no errors, deploy a new schema.
	 * @param stream
	 * @return
	 */
	public ParserResponse parseSchema(InputStream stream);
	
	/**
	 * Parse and, if no errors, deploy a new test schema.
	 * @param string
	 * @return
	 */
	public ParserResponse parseSchema(String string);

	/**
	 * Get currently deployed test schema.
	 * @return Current test schema or null, if none has been deployed yet.
	 */
	public Schema getSchema();
	
	/**
	 *  Get user's Variant session. 
	 *  
	 * @param create   Whether or not create the session if does not exist.
	 * @param sessionIdPersisterUserData Opaque object that will be passed without
	 *                 interpretation to <code>SessionIdPersister.get()</code>.
	 * @return          
	 */
	public VariantSession getSession(boolean create, Object sessionIdPersisterUserData);
	
	/**
	 * Get user's Variant session. 
	 * 
	 * @param sessionIdPersisterUserData Opaque object that will be passed without
	 *                 interpretation to <code>SessionIdPersister.get()</code>.
	 * @return
	 */
	public VariantSession getSession(Object sessionIdPersisterUserData);
	
	/**
     * Start view Request 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public VariantViewRequest startViewRequest(VariantSession session, View view);
	
	/**
	 * Commit of a view request.
	 * @param request
	 */
	public void commitViewRequest(VariantViewRequest request, Object sessionIdPersisterUserData);
		
	/**
	 * Factory singleton class.
	 */
	public static class Factory {
		private static Variant instance = null;
		public static Variant getInstance() {
			if (instance == null) {
				instance = new VariantCoreImpl();
			}
			return instance;
		}
	}
}
