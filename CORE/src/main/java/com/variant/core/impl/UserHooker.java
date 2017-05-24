package com.variant.core.impl;

import com.variant.core.HookListener;
import com.variant.core.UserHook;

/**
 * User Hook processor.
 * 
 * @author
 *
 */
public interface UserHooker {

	/**
	 * Add a single custom hook listener to this hooker.
	 */
	void addListener(@SuppressWarnings("unchecked") HookListener<? extends UserHook>... listener);
	
	/**
	 * Remove all custom hook listeners. 
	 */
	void clear();

	/**
	 * Post all listeners for a particular hook type.
	 * @param hook
	 * @return the hook passed in as argument.
	 */
	public UserHook post(UserHook hook);

	/**
	 * Null Hooker which does nothing whatsoever.
	 * Good enough for core tests and for the client side (there are no hooks on the client).
	 * @author Igor
	 *
	 */
	public static class Null implements UserHooker {
		
		@Override
		public void addListener(@SuppressWarnings("unchecked") HookListener<? extends UserHook>... listener) {}
		
		@Override
		public void clear() {}

		@Override
		public UserHook post(UserHook hook) {return hook;}
	}
}

