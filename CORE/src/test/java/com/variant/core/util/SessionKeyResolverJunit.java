package com.variant.core.util;

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
		
	/**
	 * Set and get method.  If args[0] is given, we'll stash the value so that
	 * the next get w/o args will retrieve it.
	 */
	@Override
	public String getSessionKey(UserData...args) {
		
		if (args != null && args.length > 0) {
			key = (String) ((UserDataJunit) args[0]).getData();
		}
		return key;
	}

	/**
	 * 
	 * @author Igor
	 *
	 */
	public static class UserDataJunit implements UserData {

		private String key;
		
		public UserDataJunit(String key) {
			this.key = key;
		}
		
		public String getData() {
			return key;
		}
		
	}
}
