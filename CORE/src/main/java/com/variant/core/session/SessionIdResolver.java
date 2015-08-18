package com.variant.core.session;

/**
 * An implementation will use external mechanisms to obtain
 * a stable session ID. For instance, in a web application environment,
 * session ID is persisted in an HTTP cookie, just like an HTTP Session ID.
 * 
 * @author Igor
 */

public interface SessionIdResolver {

	/**
	 * 
	 * @param userArgs in an environment like servlet container, user arg will be http request.
	 * 
	 * @return
	 */
	String getSessionId(Object userData);
	
}
