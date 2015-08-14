package com.variant.core.ext;

import com.variant.core.session.SessionKeyResolver;

/**
 * Sample implementation of the SessionKeyResolver.
 * Caches and returns the last value passed to the getSessionKey() method
 * as a UserData argument. Not likely this is suitable for anything more
 * than JUnits.
 * 
 * @author Igor.
 *
 */
public class SessionKeyResolverSample implements SessionKeyResolver {

	private String key = null;
		
	/**
	 * Set and get method.  If args[0] is given, we'll stash the value so that
	 * the next get w/o args will retrieve it.
	 */
	@Override
	public String getSessionKey(UserData...args) {
		
		if (args != null && args.length > 0) {
			key = (String) ((UserDataSample) args[0]).getData();
		}
		return key;
	}

	/**
	 * 
	 * @author Igor
	 *
	 */
	public static class UserDataSample implements UserData {

		private String key;
		
		public UserDataSample(String key) {
			this.key = key;
		}
		
		public String getData() {
			return key;
		}
		
	}
}
