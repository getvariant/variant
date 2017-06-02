package com.variant.server.schema;


import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.CommonError;
import com.variant.core.LifecycleEvent;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.server.api.ServerException;
import com.variant.server.api.UserHook;
import com.variant.server.boot.ServerErrorLocal;

/**
 * User Hook processor
 * 
 * @author
 *
 */
public class ServerHooker implements UserHooker {

	private static final Logger LOG = LoggerFactory.getLogger(UserHooker.class);

	/*
	 *  Initialized hooks are stored in a linked map, keyed by the schema hook, sorted in ordinal order,
	 *  i.e. in the order they were declared in the meta section. Note that schema hook impl overrides equal()
	 *  to compare hook names.
	 *  The map entry contains the hooks class, which will be used to spawn new instances quickly and the
	 *  LCE event.
	 */
	
	private static class HookMapEntry {
		private Class<UserHook<LifecycleEvent>> hookClass;
		private Class<? extends LifecycleEvent> lceClass;
		private HookMapEntry(Class<UserHook<LifecycleEvent>> hookClass, Class<? extends LifecycleEvent> lceClass) {
			this.hookClass = hookClass;
			this.lceClass = lceClass;
		}
	}
	private LinkedHashMap<Hook, HookMapEntry> hookMap = new LinkedHashMap<Hook, HookMapEntry>();

	
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
	public void initHook(Hook hook, ParserResponseImpl parserResponse) {
		
		try {
			Class<?> userHookClass = Class.forName(hook.getClassName());
			Object userHookObject = userHookClass.newInstance();
			if (! (userHookObject instanceof UserHook)) {
				parserResponse.addMessage(ServerErrorLocal.HOOK_CLASS_NO_INTERFACE, UserHook.class.getName());
			}
			UserHook<? extends LifecycleEvent> userHook = (UserHook<LifecycleEvent>) userHookObject;
			HookMapEntry hme = new HookMapEntry((Class<UserHook<LifecycleEvent>>) userHookClass, userHook.getLifecycleEventClass());
			hookMap.put(hook, hme);
		}
		catch (Exception e) {
			LOG.error(ServerErrorLocal.HOOK_INSTANTIATION_ERROR.asMessage(hook.getClassName(), e.getClass().getName()), e);
			parserResponse.addMessage(ServerErrorLocal.HOOK_INSTANTIATION_ERROR, hook.getClassName(), e.getClass().getName());
		}

	}
	
	/**
	 * 
	 *
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
		for (Map.Entry<Hook, HookMapEntry> entry : hookMap.entrySet()) {
			Hook schemaHook = entry.getKey();
			HookMapEntry hme = entry.getValue();
			if (hme.lceClass.isAssignableFrom(event.getClass())) {
				UserHook<LifecycleEvent> hook;
				try {
					hook = hme.hookClass.newInstance();
					hook.post(event, schemaHook);
				} catch (Exception e) {
					throw new ServerException.User(CommonError.HOOK_UNHANDLED_EXCEPTION, UserHook.class.getName());
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Posted user hook [" + schemaHook.getName() + "]");
			}
		}
		return event;
	}

}
