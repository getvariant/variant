package com.variant.core.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.HookListener;
import com.variant.core.UserHook;

/**
 * User Hook processor
 * 
 * @author
 *
 */
public class UserHooker {

	private static final Logger LOG = LoggerFactory.getLogger(UserHooker.class);

	private ArrayList<HookListener<? extends UserHook>> listeners = 
			new ArrayList<HookListener<? extends UserHook>>();

	/**
	 * Package instantiation only.
	 */
	public UserHooker() {}
	
	/**
	 * 
	 * @param listener
	 */
	@SuppressWarnings("unchecked") 
	public void addListener(HookListener<? extends UserHook>... listeners) {
		for (int i = 0; i < listeners.length; i++)
			this.listeners.add(listeners[i]);
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
	 * @return the hook passed in as argument.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public UserHook post(UserHook hook) {
		for (HookListener listener: listeners) {
			if (listener.getHookClass().isInstance(hook)) {
				listener.post(hook);
				if (LOG.isTraceEnabled())
					LOG.trace("Posted user hook [" + hook.getClass().getName() + "]");
			}
		}
		return hook;
	}

}

