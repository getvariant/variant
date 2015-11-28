package com.variant.core;


/**
 * <p>An implementation will use external mechanisms to obtain and to store
 * the session ID between state resolution requests. For instance, in a 
 * web application environment, session ID tracked in an HTTP cookie, 
 * just like HTTP Session ID. This mechanism is typically different from
 * the storage of the session itself.
 * 
 * @see VariantSessionStore
 * @author Igor Urisman
 * @since 0.5
 */

public interface VariantSessionIdTracker {

	/**
	 * <p>Retrieve the session ID from the tracker. If the session ID did not exist,
	 * the implementation should create it and, if needed, save it in the tracker.
	 *  
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#newStateRequest(VariantSession, com.variant.core.schema.State, Object)} 
	 *                 will pass here without interpretation.
	 * 
	 * @return Session ID. Should never be null.
	 * @see com.variant.core.Variant#newStateRequest(VariantSession, com.variant.core.schema.State, Object).
	 * @since 0.5
	 */
	public String get(Object...userData);

}

