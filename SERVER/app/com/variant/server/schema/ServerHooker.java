package com.variant.server.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.variant.core.CommonError;
import com.variant.core.LifecycleEvent;
import com.variant.core.LifecycleEvent.Domain;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.EventDomain;
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
		private ConfigValue config;
		private HookMapEntry(Class<UserHook<LifecycleEvent>> hookClass, Class<? extends LifecycleEvent> lceClass, ConfigValue config) {
			this.hookClass = hookClass;
			this.lceClass = lceClass;
			this.config = config;
		}
	}
	private LinkedHashMap<Hook, HookMapEntry> hookMap = new LinkedHashMap<Hook, HookMapEntry>();

	
	/**
	 * Package instantiation only.
	 */
	ServerHooker() {}
	
	/**
	 * Initialize a schema hook
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initHook(Hook hook, ParserResponseImpl parserResponse) {
				
		try {
			// Create the Class object for the supplied UserHook implementation.
			Class<?> userHookClass = Class.forName(hook.getClassName());
			Object userHookObject = userHookClass.newInstance();
			
			// It must implement the right interface.
			if (! (userHookObject instanceof UserHook)) {
				parserResponse.addMessage(ServerErrorLocal.HOOK_CLASS_NO_INTERFACE, UserHook.class.getName());
			}
			UserHook<? extends LifecycleEvent> userHook = (UserHook<LifecycleEvent>) userHookObject;
			
			// The implementation's domian must match that of the definition. In other words, a test domain hook
			// cannot be defined at the schema level, and vice versa.
			EventDomain eventDomain = userHook.getLifecycleEventClass().getAnnotation(EventDomain.class);
			if (hook instanceof Hook.Test) {
				if (eventDomain.value() == Domain.SCHEMA) {
					Hook.Test testHook = (Hook.Test) hook;
					parserResponse.addMessage(
							ServerErrorLocal.HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST, 
							hook.getName(), userHook.getLifecycleEventClass().getName(), testHook.getTest().getName());
				}
			}
			else if (hook instanceof Hook.Schema) {
				if (eventDomain.value() == Domain.TEST) {
					parserResponse.addMessage(ServerErrorLocal.HOOK_TEST_DOMAIN_DEFINED_AT_SCHEMA, hook.getName(), userHook.getLifecycleEventClass().getName());
				}
			}
			else {
				throw new ServerException.Internal(String.format("Unexpected hook class [%s]", hook.getClassName()));
			}
			
			// Parse init JSON string
			ConfigValue config = null;
			if (hook.getInit() != null) {
				config = ConfigFactory.parseString("{init:"  + hook.getInit() + "}").getValue("init"); 
			}

			// AOK. Save in hook map.
			hookMap.put(hook, new HookMapEntry((Class<UserHook<LifecycleEvent>>) userHookClass, userHook.getLifecycleEventClass(), config));

		}
		catch (ConfigException.Parse e) {
			parserResponse.addMessage(ServerErrorLocal.HOOK_INSTANTIATION_ERROR, hook.getClassName(), e.getClass().getName());
		}
		catch (Exception e) {
			LOG.error(ServerErrorLocal.HOOK_INSTANTIATION_ERROR.asMessage(hook.getClassName(), e.getClass().getName()), e);
			parserResponse.addMessage(ServerErrorLocal.HOOK_INSTANTIATION_ERROR, hook.getClassName(), e.getClass().getName());
		}

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
					hook.init(hme.config);
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
