package com.variant.core;

import com.variant.core.exception.VariantException;


/**
 * <p>An implementation will use external mechanisms to obtain and to store
 * Variant session between state resolution requests. Typically, this mechanism
 * will be different from that used to track session ID. The implementation
 * must also supply an implementation of {@link VariantSessionIdTracker}
 * 
 * @see VariantSessionIdTracker
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantSessionStore {
	
	/**
	 * <p>The container will call this method immediately following the instantiation with
	 * the init parameter map, as specified by the <code>session.store.class.init</code>
	 * application property. 
	 * 
	 * @since 0.5
	 */
	public void initialized(VariantCoreInitParams initParams) throws Exception ;

	/**
	 * Get a session from the store by its ID. 
	 * @param sessionId The ID of the session of interest.
	 * @return An instance of {@link VariantCoreSession} if session with this ID was
	 *         found in the store, or null if not.
	 * @since 0.5
	 */
	public VariantCoreSession get(String sessionId) throws VariantException;

	/**
	 * Save the session.
	 * @param session The session to be saved in the store.
	 * @since 0.5
	 */
	public void save(VariantCoreSession session) throws VariantException;	
		
	/**
	 * Shutdown this session store. Releases all resources. All subsequent calls to this session store will
	 * result in an exception.
	 * 
	 * @since 0.5
	 */
	public void shutdown();

}
