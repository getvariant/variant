package com.variant.core.ext;

import com.variant.core.session.SessionIdResolver;

/**
 * Sample implementation of the SessionIdResolver.
 * Caches and returns the last value passed to the getSessionKey() method
 * as a UserData argument. Not likely this is suitable for anything more
 * than JUnits.
 * 
 * @author Igor.
 *
 */
public class SessionIdResolverSample implements SessionIdResolver {

	private String id = null;
		
	/**
	 * Set and get method.  userData is expected to the the String ID.
	 * If passed, we'll stash the value so that the next get with null
	 * userData will retrieve it.
	 */
	@Override
	public String getSessionId(Object userData) {
		
		if (userData != null) id = (String) userData;
		return id;
	}

}
