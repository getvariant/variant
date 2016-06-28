package com.variant.core.impl;

import java.util.ArrayList;

import com.variant.core.hook.HookListener;
import com.variant.core.hook.UserHook;

/**
 * User Hook processor
 * 
 * @author
 *
 */
public class UserHooker {

	private ArrayList<HookListener<? extends UserHook>> listeners = 
			new ArrayList<HookListener<? extends UserHook>>();

	/**
	 * Package instantiation only.
	 */
	UserHooker() {}
	
	/**
	 * 
	 * @param listener
	 */
	public void addListener(HookListener<? extends UserHook> listener) {
		listeners.add(listener);
	}
	
	/**
	 * 
	 */
	public void clear() {
		listeners.clear();
	}

	/**
	 * Post all listeners listening on a particular hook.
	 * @param listenerClass
	 * @param hook
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void post(UserHook hook) {
		for (HookListener listener: listeners) {
			if (listener.getHookClass().isInstance(hook))
				listener.post(hook);
		}
	}
}

