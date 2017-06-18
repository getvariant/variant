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
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParseTimeLifecycleEvent;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.server.api.ServerException;
import com.variant.server.api.TestScopedLifecycleEvent;
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
		private Class<UserHook<LifecycleEvent>> hookClass;  // Span a new instance for each post()
		private Class<? extends LifecycleEvent> lceClass;   // post() for any subclass 
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
			
			// The implementation's scope must match that of the definition. In other words, a test scoped hook
			// cannot be defined at the schema level, and vice versa.
			if (hook instanceof Hook.Test) {
				if (ParseTimeLifecycleEvent.class.isAssignableFrom(userHook.getLifecycleEventClass())) {
					Hook.Test testHook = (Hook.Test) hook;
					parserResponse.addMessage(
							ServerErrorLocal.HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST, 
							hook.getName(), userHook.getLifecycleEventClass().getName(), testHook.getTest().getName());
				}
			}
			else if (hook instanceof Hook.Schema) {
				if (TestScopedLifecycleEvent.class.isAssignableFrom(userHook.getLifecycleEventClass())) {
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

			LOG.debug("Init'ed Hook [" + hook.getName() + "]");
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
			
			// Only post subscribers to the event type.
			if (hme.lceClass.isAssignableFrom(event.getClass())) {
				
				// Test scoped events only post for hooks defined within the scope of the respective test.
				if (event instanceof TestScopedLifecycleEvent &&
					!((TestScopedLifecycleEvent) event).getTest().equals(((Hook.Test)schemaHook).getTest())) continue;
				
				try {
					UserHook<LifecycleEvent> hook = hme.hookClass.newInstance();
					Config config = hme.config == null ? null : hme.config.atKey("init");
					hook.init(config, schemaHook);
					hook.post(event);
				
				} catch (ServerException.User e) {
					throw e;
				
				} catch (Exception e) {
					LOG.error(CommonError.HOOK_UNHANDLED_EXCEPTION.asMessage(UserHook.class.getName(), e.getMessage()), e);
					throw new ServerException.User(CommonError.HOOK_UNHANDLED_EXCEPTION, UserHook.class.getName(), e.getMessage());
				}

				if (LOG.isTraceEnabled())
					LOG.trace("Posted user hook [" + schemaHook.getName() + "] with [" + event + "]");
				
			}
		}
		return event;
	}

}
