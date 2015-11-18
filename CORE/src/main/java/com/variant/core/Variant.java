package com.variant.core;

import java.io.InputStream;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.flashpoint.Flashpoint;
import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

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
	 * Is the Variant engine bootstrapped.
	 * @return
	 */
	
	public boolean isBootstrapped();
	
	/**
	 * Programmatically shutdown Variant engine.
	 */
	public void shutdown();
	
	/**
	 * Client code may extend default semantics by supplying flashpoint listeners.
	 * 
	 * @param listener
	 */
	public void addFlashpointListener(FlashpointListener<? extends Flashpoint> listener);
	

	/**
	 * Remove all current flashpoint listeners.
	 * 
	 * @param listener
	 */
	public void clearFlashpointListeners();

	/**
	 * Parse and, if no errors, optionally deploy a new schema.
	 * @param stream
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public ParserResponse parseSchema(InputStream stream, boolean deploy);
			
	/**
	 * Parse and, if no errors, deploy a new schema.  Equivalent to parseSchema(stream, true).
	 * @param stream
	 * @return
	 */
	public ParserResponse parseSchema(InputStream stream);
	
	/**
	 * Get currently deployed test schema.
	 * @return Current test schema or null, if none has been deployed yet.
	 */
	public Schema getSchema();
		
	/**
	 * Get user's Variant session. Will be created if doesn't yet exist;
	 * 
	 * @param sessionIdPersisterUserData Opaque object that will be passed without
	 *                 interpretation to <code>SessionIdPersister.get()</code>.
	 * @return
	 */
	public VariantSession getSession(Object sessionIdPersisterUserData);
	
	/**
     * Start state Request 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public VariantStateRequest newStateRequest(VariantSession session, State state, Object targetingPersisterUserData);
	
	/**
	 * Commit of a view request.
	 * @param request
	 */
	public void commitStateRequest(VariantStateRequest request, Object sessionIdPersisterUserData);
		
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
