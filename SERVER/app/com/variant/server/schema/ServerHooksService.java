package com.variant.server.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.variant.core.CommonError;
import com.variant.core.EventFlusher;
import com.variant.core.lce.LifecycleEvent;
import com.variant.core.lce.ParsetimeLifecycleEvent;
import com.variant.core.lce.StateAwareLifecycleEvent;
import com.variant.core.lce.TestAwareLifecycleEvent;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.HooksService;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.server.api.ServerException;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantClassLoader;

/**
 * User Hook processor
 * 
 * @author
 *
 */
public class ServerHooksService implements HooksService {

	private static final Logger LOG = LoggerFactory.getLogger(ServerHooksService.class);

	/*
	 *  Initialized hooks are stored in a linked map, keyed by the schema hook, sorted in ordinal order,
	 *  i.e. in the order they were declared in the meta section. Note that schema hook impl overrides equal()
	 *  to compare hook names.
	 *  The map entry contains the hooks class, which will be used to spawn new instances quickly and the
	 *  LCE event.
	 */
	
	private static class HookListEntry {
		private UserHook<? extends LifecycleEvent> hookImpl;       // Hook object will cloned for each post() just in case the implementation is mutable.
		private Hook hookDef;                                      // Schema definition of the hook.
		private HookListEntry(Hook hookDef, UserHook<? extends LifecycleEvent> hookImpl) {
			this.hookDef = hookDef;
			this.hookImpl = hookImpl;
		}
	}
	
	// Schema scoped hooks, in ordinal order
	private ArrayList<HookListEntry> schemaHooks = new ArrayList<HookListEntry>();

	// State scoped hooks, in ordinal order
	private ArrayList<HookListEntry> stateHooks = new ArrayList<HookListEntry>();
	
	// Test scoped hooks, in ordinal order
	private ArrayList<HookListEntry> testHooks = new ArrayList<HookListEntry>();

	
	/**
	 * Package instantiation only.
	 */
	ServerHooksService() {}
	
	/**
	 * Initialize a schema hook
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initHook(Hook hookDef, ParserResponseImpl parserResponse) {
		
		try {
			// Create the Class object for the supplied UserHook implementation.
			Object hookObj = VariantClassLoader.instantiate(hookDef.getClassName(), hookDef.getInit());
					
			if (hookObj == null) {
				parserResponse.addMessage(ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR, hookDef.getClassName());
				return;
			}
			
			// It must implement the right interface.
			if (! (hookObj instanceof UserHook)) {
				parserResponse.addMessage(ServerErrorLocal.OBJECT_CLASS_NO_INTERFACE, hookObj.getClass().getName(), UserHook.class.getName());
				return;
			}
			
			UserHook<? extends LifecycleEvent> hookImpl = (UserHook<LifecycleEvent>) hookObj;
			
			// The implementation's scope must be compatible with that of the definition:
			// 1. Test-scoped hookDef must define an implementation which listens to a test aware event.
			if (hookDef instanceof Hook.State && ! StateAwareLifecycleEvent.class.isAssignableFrom(hookImpl.getLifecycleEventClass())) {
				parserResponse.addMessage(
						ServerErrorLocal.HOOK_STATE_SCOPE_VIOLATION, 
						hookDef.getName(), ((Hook.State)hookDef).getState().getName(), hookImpl.getLifecycleEventClass().getName());				
			}
			// 2. State-scoped hookDef must define an implementation which listens to a state aware event.
			if (hookDef instanceof Hook.Test && ! TestAwareLifecycleEvent.class.isAssignableFrom(hookImpl.getLifecycleEventClass())) {
				parserResponse.addMessage(
						ServerErrorLocal.HOOK_TEST_SCOPE_VIOLATION, 
						hookDef.getName(), ((Hook.Test)hookDef).getTest().getName(), hookImpl.getLifecycleEventClass().getName());				
			}

						
			// AOK. Add to apropriate hook list.
			if (hookDef instanceof Hook.Schema) schemaHooks.add(new HookListEntry(hookDef, hookImpl));
			else if (hookDef instanceof Hook.State) stateHooks.add(new HookListEntry(hookDef, hookImpl));
			else if (hookDef instanceof Hook.Test) testHooks.add(new HookListEntry(hookDef, hookImpl));

		}
		catch (ConfigException.Parse e) {
			parserResponse.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, hookDef.getClassName(), e.getClass().getName());
		}
		catch (Exception e) {
			LOG.error(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage(hookDef.getClassName(), e.getClass().getName()), e);
			parserResponse.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, hookDef.getClassName(), e.getClass().getName());
		}

	}
	
	/**
	 * Post all hooks listening on a particular LSE.
	 * @param listenerClass
	 * @param hook
	 * @return the hook passed in as argument.
	 */
   @SuppressWarnings("unchecked")
   @Override
   public UserHook.PostResult post(LifecycleEvent event) {
			 

	   try {

			for (HookListEntry hle : hookList) {
								
				// Only post subscribers to the event type.
				if (hle.hookImpl.getLifecycleEventClass().isAssignableFrom(event.getClass())) {
					
					// Test scoped events post for test-scoped hooks first, and then for schema scoped hooks.
					if (event instanceof TestAwareLifecycleEvent &&
						!((TestAwareLifecycleEvent) event).getTest().equals(((Hook.Test)schemaHook).getTest())) continue;
					
					UserHook<LifecycleEvent> hook = hme.hookClass.newInstance();
					Config config = hme.config == null ? null : hme.config.atKey("init");
					hook.init(config, schemaHook);
					UserHook.PostResult result = hook.post(event);
				
					if (LOG.isTraceEnabled())
						LOG.trace("Posted user hook [" + schemaHook.getName() + "] with [" + event + "]. Result: [" + result + "]");
					
					// If user hook returned a result, clip the chain.
					if (result != null) return result;
	
				}	
	
			}
			
			// Either no hooks listening for this event, or none cared to return a result.
			// Post default hook.
			// (I don't understand the need form this cast)
			return ((UserHook<LifecycleEvent>)event.getDefaultHook()).post(event);

		} catch (ServerException.User e) {
			throw e;
		
		} catch (Exception e) {
			LOG.error(CommonError.HOOK_UNHANDLED_EXCEPTION.asMessage(hme.hookClass.getName(), e.getMessage()), e);
			throw new ServerException.User(CommonError.HOOK_UNHANDLED_EXCEPTION, UserHook.class.getName(), e.getMessage());
		}				

	}

}
