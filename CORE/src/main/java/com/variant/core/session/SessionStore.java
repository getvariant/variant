package com.variant.core.session;

import com.variant.core.VariantSession;
import com.variant.core.net.SessionPayloadReader;
import com.variant.core.util.inject.Injectable;


/**
 * <p>An implementation will use external mechanisms to obtain and to store
 * Variant session between state resolution requests. Typically, this mechanism
 * will be different from that used to track session ID. The implementation
 * must also supply an implementation of {@link VariantSessionIdTracker}
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface SessionStore extends Injectable {
	
	/**
	 * Get a session from the store by its ID. 
	 * @param sessionId The ID of the session of interest.
	 * @return An instance of {@link VariantSession} if session with this ID was
	 *         found in the store, or null if not.
	 * @since 0.5
	 */
	public SessionPayloadReader get(String sessionId, boolean create);

	/**
	 * Save the session.
	 * @param session The session to be saved in the store.
	 * @since 0.5
	 */
	public void save(VariantSession session);	

}
