package com.variant.core;

/**
 * Session store holds on to the variant session between variant requests. 
 * Implementations will be distributed, suitable for a particular application.
 * Testing of most of the code code could be done with a simple local hash map.
 * 
 * @author Igor
 *
 */
public interface VariantSessionStore {
	
	/**
	 * Save the session.
	 * @param session The session to be saved in the store.
	 * @param userData Opaque object that was passed to 
	 */
	public void save(VariantSession session, Object...userData);	

	/**
	 * 
	 * @param userData Opaque object that was passed to Variant.getSession()
	 * @return
	 */
	public VariantSession get(Object...userData);
	
	/**
	 * Each session store implements its own SID tracker.
	 * @return
	 */
	VariantSessionIdTracker getSessionIdTracker();
	
	/**
	 * Release memory resources, if no longer needed.
	 */
	public void shutdown();
	
}
