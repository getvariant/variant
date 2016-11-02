package com.variant.client;

import com.variant.core.VariantCoreSession;
import com.variant.core.event.VariantEvent;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.xdm.State;

/**
 * <p>Represents a Variant user session. Variant has its own notion of user session, 
 *    independent from that of the host application's. Variant session provides a way to 
 *    identify the user across multiple state requests and contains session-scoped application 
 *    state that must be preserved between state requests. Variant server acts as the session 
 *    store by maintaining a map of user session objects keyed by session ID.
 *    Variant maintains its own session, rather than relying on the host application's, 
 *    because it is frequently desirable for Variant session to survive the destruction 
 *    of the host application's session.
 * 
 * <p> Variant session expires when either a configurable session timeout period of inactivity,
 *     or after a schema redeployment. Once the session represented by this object has expired,
 *     all subsequent operations on it, apart from {@code getId()} and {@code isExpired()} will throw 
 *     {@link VariantRuntimeUserErrorException}.
 *
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface VariantSession extends VariantCoreSession {

	/**
     * <p>Target session for a state. 
     *  
	 * @return An instance of the {@link VariantStateRequest} object, which
	 *         may be further examined for more information about the outcome of this operation.  
	 *
	 * @since 0.5
	 */
	VariantStateRequest targetForState(State state);
	
	/**
     * <p>The Variant client instance that created this session. 
     *  
	 * @return An instance of the {@link VariantClient} object, which originally created this object
	 *         via {@link VariantClient#getSession(Object...)}.
	 *
	 * @since 0.6
	 */
	VariantClient getClient();
	
	/**
	 * Trigger a custom event.
	 * 
	 * @param An implementation of {@link VariantEvent} which represents the custom event to be triggered.
	 * @since 0.7
	 */
	public void triggerEvent(VariantEvent event);
		
	/**
	 * <p>Indicates whether this session has expired. A session expires either after it has
	 * been inactive for the period of time configured by the {@code session.timeout.secs}
	 * system property, or after the schema which was in effect during its creation, has 
	 * been undeployed.
	 * 
	 * @return true if this session has expired or false otherwise.
	 * @since 0.6
	 */
	public boolean isExpired();

	/**
	 * <p>Set a session-scoped attribute.
	 * 
	 * @return The object which was previously associated with this attribute, or null if none.
	 * @since 0.6
	 */
	public Object setAttribute(String name, Object value);
	
	/**
	 * <p>Retrieve a session-scoped attribute.
	 * 
	 * @return The object associated with this attribute.
	 * @since 0.6
	 */
	public Object getAttribute(String name);

}
