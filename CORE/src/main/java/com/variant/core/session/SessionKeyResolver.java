package com.variant.core.session;

/**
 * An implementation will use external mechanisms to obtain
 * a stable session key. For instance, in a web application environment,
 * session key is persisted in a browser cookie.
 * 
 * @author Igor
 */

public interface SessionKeyResolver {

	String getSessionKey();
}
