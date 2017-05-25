package com.variant.server.boot;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.LifecycleEvent;
import com.variant.core.UserHook;
import com.variant.core.impl.UserHooker;

/**
 * User Hook processor
 * 
 * @author
 *
 */
public class ServerHooker  implements UserHooker {

	private static final Logger LOG = LoggerFactory.getLogger(UserHooker.class);

	private ArrayList<UserHook<? extends LifecycleEvent>> hooks = new ArrayList<UserHook<? extends LifecycleEvent>>();

	/**
	 * Package instantiation only.
	 */
	ServerHooker() {}
	
	/**
	 * Add one or more user hooks.
	 * 
	 * @param hooks
	 */
	@Override
	public void addHook(@SuppressWarnings("unchecked") UserHook<? extends LifecycleEvent>... hooks) {
		for (int i = 0; i < hooks.length; i++)
			this.hooks.add(hooks[i]);
	}
	
	/**
	 * 
	 */
	@Override
	public void clear() {
		hooks.clear();
	}

	/**
	 * Post all hooks listening on a particular LSE.
	 * @param listenerClass
	 * @param hook
	 * @return the hook passed in as argument.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LifecycleEvent post(LifecycleEvent event) {
		for (UserHook hook: hooks) {
			if (hook.getLifecycleEventClass().isInstance(event)) {
				hook.post(event);
				if (LOG.isTraceEnabled())
					LOG.trace("Posted user hook [" + hook.getClass().getName() + "]");
			}
		}
		return event;
	}

}
