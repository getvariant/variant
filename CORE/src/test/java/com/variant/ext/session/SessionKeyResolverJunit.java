package com.variant.ext.session;

import com.variant.core.session.SessionKeyResolver;

/**
 * Simple implementation of the SessionKeyResolver interface suitable for JUnit tests.
 * Allows client code to (re)set a key and then returns it.
 * 
 * @author Igor.
 *
 */
public class SessionKeyResolverJunit implements SessionKeyResolver {

	private String key = null;
	
	public void setSessionKey(String key) {
		this.key = key;
	}
	
	@Override
	public String getSessionKey() {
		return key;
	}

}
