package com.variant.client;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.schema.State;

/**
 * <p>Represents a Variant user session. Variant has its own notion of user session, 
 *    independent from that of the host application. Variant session provides a way to 
 *    identify the user across multiple state requests and contains session-scoped application 
 *    state that must be preserved between state requests. Variant server acts as the session 
 *    store by maintaining a map of user session objects keyed by session ID.
 *    Variant maintains its own session, rather than relying on the host application’s, 
 *    because it is frequently desirable for Variant session to survive the destruction 
 *    of the host application’s session.
 * </p>
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantSession extends VariantCoreSession {

	/**
     * <p>Target session for a state. Overrides core's return type.
     *  
	 * @return An instance of the {@link com.variant.core.VariantStateRequest} object, which
	 *         may be further examined for more information about targeting.  
	 *
	 * @since 0.5
	 */
	@Override
	public VariantStateRequest targetForState(State state);

	/**
	 * <p>Get most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 */
	public VariantStateRequest getStateRequest();

	/**
	 * <p>Has this session expired? An expired session cannot be used: calling any method on it
	 * will throw the {@link VariantRuntimeUserErrorException}</p>
	 * 
	 * @return true if this session has expired or false otherwise.
	 */
	public boolean isExpired();
}
