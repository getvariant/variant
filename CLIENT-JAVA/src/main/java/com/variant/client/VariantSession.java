package com.variant.client;

import com.variant.core.VariantCoreSession;

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

}
