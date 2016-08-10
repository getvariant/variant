package com.variant.client;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.schema.State;

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
	
}
