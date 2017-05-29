package com.variant.server.boot;


import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.LifecycleEvent;
import com.variant.core.ServerError;
import com.variant.core.UserHook;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Hook;
import com.variant.server.ServerException;

/**
 * User Hook processor
 * 
 * @author
 *
 */
public class ServerHooker implements UserHooker {

	private static final Logger LOG = LoggerFactory.getLogger(UserHooker.class);

	/*
	 *  Initialized hooks are stored in a linked map, keyed by the hook name, sorted in ordinal order,
	 *  i.e. in the order they were declared in the meta section.
	 *  The map entry contains the hooks class, which will be used to span new instances quckly and the
	 *  LCE class for which this hook listens.
	 */
	
	private static class HookMapEntry {
		private Class<UserHook<LifecycleEvent>> hookClass;
		private Class<? extends LifecycleEvent> eventClass;
		private HookMapEntry(Class<UserHook<LifecycleEvent>> hookClass, Class<? extends LifecycleEvent> eventClass) {
			this.hookClass = hookClass;
			this.eventClass = eventClass;
		}
	}
	private LinkedHashMap<String, HookMapEntry> hookMap = new LinkedHashMap<String, HookMapEntry>();

	
	/**
	 * Package instantiation only.
	 */
	ServerHooker() {}
	
	/**
	 * Add one or more user hooks.
	 * 
	 * @param hooks
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initHook(Hook hook) {
		
		try {
			Class<?> userHookClass = Class.forName(hook.getClassName());
			Object userHookObject = userHookClass.newInstance();
			if (! (userHookObject instanceof UserHook)) {
				throw new ServerException.User(ServerError.HookClassNoInterface, UserHook.class.getName());
			}
			UserHook<? extends LifecycleEvent> userHook = (UserHook<LifecycleEvent>) userHookObject;
			HookMapEntry hme = new HookMapEntry((Class<UserHook<LifecycleEvent>>) userHookClass, userHook.getLifecycleEventClass());
			hookMap.put(hook.getName(), hme);
		}
		catch (Exception e) {
			throw new ServerException.Internal("Unable to instantiate hook class [" + hook.getClassName() + "]", e);
		}

	}
	
	/**
	 * 
	 */
	@Override
	public void clear() {
		hookMap.clear();
	}

	/**
	 * Post all hooks listening on a particular LSE.
	 * @param listenerClass
	 * @param hook
	 * @return the hook passed in as argument.
	 */
	@Override
	public LifecycleEvent post(LifecycleEvent event) {
		for (Map.Entry<String, HookMapEntry> entry : hookMap.entrySet()) {
			String name = entry.getKey();
			HookMapEntry hme = entry.getValue();
			if (hme.eventClass.isAssignableFrom(event.getClass())) {
				UserHook<LifecycleEvent> hook;
				try {
					hook = hme.hookClass.newInstance();
					hook.post(event);
				} catch (Exception e) {
					throw new ServerException.Internal("Unhandled exception in user hook [" + name + "]");
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Posted user hook [" + name + "]");
			}
		}
		return event;
	}

}
