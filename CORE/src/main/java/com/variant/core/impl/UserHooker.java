package com.variant.core.impl;

import com.variant.core.UserHook;
import com.variant.core.LifecycleEvent;

/**
 * User Hook processor.
 * 
 * @author
 *
 */
public interface UserHooker {

	/**
	 * Add user hooks to this hooker.
	 */
	void addHook(@SuppressWarnings("unchecked") UserHook<? extends LifecycleEvent>... hooks);
	
	/**
	 * Remove all user hooks. 
	 */
	void clear();

	/**
	 * Post all hooks for a particular LCE type.
	 * @param event
	 * @return the event passed in as argument.
	 */
	public LifecycleEvent post(LifecycleEvent event);

	/**
	 * Null hooker, which does nothing whatsoever.
	 * Good enough for core tests and for the client side (there are no hooks on the client).
	 * 
	 * @author Igor
	 *
	 */
	public static class Null implements UserHooker {
		
		@Override
		public void addHook(@SuppressWarnings("unchecked") UserHook<? extends LifecycleEvent>... listener) {}
		
		@Override
		public void clear() {}

		@Override
		public LifecycleEvent post(LifecycleEvent hook) {return hook;}
	}
}

