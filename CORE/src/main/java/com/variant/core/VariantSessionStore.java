package com.variant.core;

import java.util.Map;

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
	public void initialized(Map<String, String> initParams) throws Exception ;

	/**
	 * Save the session.
	 * @param session The session to be saved in the store.
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#commitStateRequest(VariantStateRequest, Object...)} 
	 *                 will pass here without interpretation. 
	 * @since 0.5
	 */
	public void save(VariantSession session, Object...userData);	

	/**
	 * Save the session.
	 * @param session The session to be saved in the store.
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#getSession(Object...)} 
	 *                 will pass here without interpretation.
	 * @since 0.5
	 */
	public VariantSession get(Object...userData);
	
	/**
	 * An implementation of {@link VariantSessionIdTracker} to be used in conjunction with this session store.
	 *
	 * @return An object of type @link VariantSessionIdTracker}.
	 * @since 0.5
	 */
	VariantSessionIdTracker getSessionIdTracker();
	
	/**
	 * Shutdown this session store. Releases all resources. All subsequent calls to this session store will
	 * result in an exception.
	 * 
	 * @since 0.5
	 */
	public void shutdown();
	
}
