package com.variant.core;

import java.io.InputStream;

import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;

/**
 * The Variant CORE API.
 * 
 * @author Igor
 *
 */
public interface Variant {


	/**
	 * Engine bootstrap with standard configuration semantics:
	 * 1. Defaults
	 * 2. Command line file.
	 * 3. Command line resource.
	 * 4. Conventional resource.
	 * 
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public void bootstrap() throws VariantBootstrapException;
	
	/**
	 * Engine bootstrap with default override configuration semantics:
	 * 1. Defaults
	 * 2. Values from the config argument override the defaults.
	 * 3. Command line file.
	 * 4. Command line resource.
	 * 5. Conventional resource.
	 * 
	 * @param config
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void bootstrap(String resourceName) throws VariantBootstrapException;	

	/**
	 * Programmatically shutdown Variant engine.
	 */
	public void shutdown();
	
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
