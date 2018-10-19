package com.variant.server.schema;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigException;
import com.variant.core.impl.ServerError;
import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.StateAwareLifecycleEvent;
import com.variant.core.lifecycle.VariationAwareLifecycleEvent;
import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.HooksService;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.server.api.ServerException;
import com.variant.server.api.lifecycle.RuntimeLifecycleEvent;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantServer;
import com.variant.server.boot.VariantServer$;
import com.variant.server.util.ClassUtil;

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
		private Class<? extends LifecycleEvent> lseClass;  // The life cycle event to which the hooks listens.
		private Hook hookDef;                              // Schema definition of the hook.
		private HookListEntry(Class<? extends LifecycleEvent> lseClass, Hook hookDef) {
			this.lseClass = lseClass;
			this.hookDef = hookDef;
		}
	}
	
	// Schema scoped hooks, in ordinal order
	private ArrayList<HookListEntry> schemaHooks = new ArrayList<HookListEntry>();

	// State scoped hooks, in ordinal order
	private ArrayList<HookListEntry> stateHooks = new ArrayList<HookListEntry>();
	
	// Test scoped hooks, in ordinal order
	private ArrayList<HookListEntry> testHooks = new ArrayList<HookListEntry>();

	// This is how we access companion objects's fields from java.
   private final VariantServer server = VariantServer$.MODULE$.instance();
		
	/**
	 * Package instantiation only.
	 */
	ServerHooksService() {}
	
	/**
	 * Initialize a schema hook
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initHook(Hook hookDef, ParserResponse parserResponse) {

		try {
			// Create the hook object for the supplied LifecycleHook implementation.
			Object hookObj = ClassUtil.instantiate(hookDef.getClassName(), hookDef.getInit());
					
			if (hookObj == null) {
				parserResponse.addMessage(ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR, hookDef.getClassName());
				return;
			}
			
			// It must implement the right interface.
			if (! (hookObj instanceof LifecycleHook)) {
				parserResponse.addMessage(ServerErrorLocal.HOOK_CLASS_NO_INTERFACE, hookObj.getClass().getName(), LifecycleHook.class.getName());
				return;
			}
			
			LifecycleHook<? extends LifecycleEvent> hookImpl = (LifecycleHook<LifecycleEvent>) hookObj;
			
			// The implementation's scope must be compatible with that of the definition:
			// 1. Test-scoped hookDef must define an implementation which listens to a test aware event.
			if (hookDef instanceof Hook.State && ! StateAwareLifecycleEvent.class.isAssignableFrom(hookImpl.getLifecycleEventClass())) {
				parserResponse.addMessage(
						ServerErrorLocal.HOOK_STATE_SCOPE_VIOLATION, 
						hookDef.getName(), ((Hook.State)hookDef).getState().getName(), hookImpl.getLifecycleEventClass().getName());				
			}
			// 2. State-scoped hookDef must define an implementation which listens to a state aware event.
			if (hookDef instanceof Hook.Variation && ! VariationAwareLifecycleEvent.class.isAssignableFrom(hookImpl.getLifecycleEventClass())) {
				parserResponse.addMessage(
						ServerErrorLocal.HOOK_TEST_SCOPE_VIOLATION, 
						hookDef.getName(), ((Hook.Variation)hookDef).getVariation().getName(), hookImpl.getLifecycleEventClass().getName());				
			}

						
			// AOK. Add to the appropriate hook list.
			HookListEntry hle = new HookListEntry(hookImpl.getLifecycleEventClass(), hookDef);

			if (hookDef instanceof Hook.Schema) {
				schemaHooks.add(hle);
				if (LOG.isDebugEnabled()) 
					LOG.debug(String.format("Registered schema-scoped hook [%s] [%s]", 
							hookDef.getName() , hookDef.getClass()));
			}
			else if (hookDef instanceof Hook.State) {
				stateHooks.add(hle);
				if (LOG.isDebugEnabled()) 
					LOG.debug(String.format("Registered state-scoped hook [%s] [%s] for state [%s]", 
							hookDef.getName() , hookDef.getClassName(), ((Hook.State)hookDef).getState()));
			}
			else if (hookDef instanceof Hook.Variation) {
				testHooks.add(hle);
				if (LOG.isDebugEnabled()) 
					LOG.debug(String.format("Registered test-scoped hook [%s] [%s] for test [%s]", 
							hookDef.getName() , hookDef.getClassName(), ((Hook.Variation)hookDef).getVariation()));
			}
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
   public LifecycleHook.PostResult post(LifecycleEvent event) {
			   	   	   
	   // 1. Build the hook chain, i.e. all hooks eligible for posting, in order.
	   
	   ArrayList<HookListEntry> chain = new ArrayList<HookListEntry>();
	   
	   if (event instanceof StateAwareLifecycleEvent) {
		   
		   for (HookListEntry hle : stateHooks) {
			   			   
			   // Only post subscribers to the event type and whose state matches.
			   if (hle.lseClass.isAssignableFrom(event.getClass()) &&
					   ((StateAwareLifecycleEvent) event).getState().equals(((Hook.State)hle.hookDef).getState())) {
				   
				   chain.add(hle);
	
				}				   
			}
	   }

	   if (event instanceof VariationAwareLifecycleEvent) {

		   for (HookListEntry hle : testHooks) {
				
			   if (hle.lseClass.isAssignableFrom(event.getClass()) &&
					   ((VariationAwareLifecycleEvent) event).getVariation().equals(((Hook.Variation)hle.hookDef).getVariation())) {
											
				   chain.add(hle);
				}				   
			}
	   }

	   for (HookListEntry hle : schemaHooks) {
		   if (hle.lseClass.isAssignableFrom(event.getClass())) {
			   chain.add(hle);
			}				   
		}

	   if (LOG.isTraceEnabled()) {
		   StringBuilder buff = new StringBuilder();
		   buff.append("Hook chain for event [").append(event.getClass().getName()).append("]: ");
		   boolean first = true;
		   for (HookListEntry hle: chain) {
			   if (first) first = false; else buff.append(", ");
			   buff.append(hle.hookDef.getName());
		   }
		   LOG.trace(buff.toString());
	   }
	   
	   // If user hook returned a result, clip the chain.
	   // 2. Post hooks on the chain, until one returns a non-null.
	   Hook hookDef = null; // Need this in the catch clause.

	   try {

		   for (HookListEntry hle : chain) {
				   
			   hookDef = hle.hookDef;
				   												
			   LifecycleHook<LifecycleEvent> hook = (LifecycleHook<LifecycleEvent>) ClassUtil.instantiate(hle.hookDef.getClassName(), hle.hookDef.getInit());
			   LifecycleHook.PostResult result = hook.post(event);
			   
			   if (LOG.isTraceEnabled())
				   LOG.trace("Posted user hook [" + hle.hookDef.getName() + "] with [" + event + "]. Result: [" + result + "]");
						
			   // If user hook returned a result, clip the chain.
			   if (result != null) return result;
		
		   }

		   // Either empty chain, or none cared to return a result.
		   // If this is a server event, post the default hook.
		   if (event instanceof RuntimeLifecycleEvent) {
			   RuntimeLifecycleEvent sle = (RuntimeLifecycleEvent) event;
			   return ((LifecycleHook<RuntimeLifecycleEvent>) sle.getDefaultHook()).post(sle);
		   }
		   
		   return null;

		} catch (ServerException.Local e) {
			throw e;
		
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(ServerError.HOOK_UNHANDLED_EXCEPTION.asMessage(hookDef.getClassName(), e.getMessage()), e);
			throw new ServerException.Remote(ServerError.HOOK_UNHANDLED_EXCEPTION, LifecycleHook.class.getName(), e.getMessage());
		}				

	}

}
