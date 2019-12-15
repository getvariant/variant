package com.variant.server.schema;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigException;
import com.variant.share.error.ServerError;
import com.variant.share.schema.Hook;
import com.variant.share.schema.MetaScopedHook;
import com.variant.share.schema.StateScopedHook;
import com.variant.share.schema.VariationScopedHook;
import com.variant.share.schema.impl.SchemaHookImpl;
import com.variant.share.schema.impl.StateScopedHookImpl;
import com.variant.share.schema.impl.VariationScopedHookImpl;
import com.variant.share.schema.parser.HooksService;
import com.variant.share.schema.parser.ParserResponse;
import com.variant.server.api.ServerException;
import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.StateAwareLifecycleEvent;
import com.variant.server.api.lifecycle.VariationAwareLifecycleEvent;
import com.variant.server.boot.ServerExceptionLocal;
import com.variant.server.boot.ServerExceptionRemote$;
import com.variant.server.boot.ServerMessageLocal;
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
				parserResponse.addMessage(ServerMessageLocal.OBJECT_CONSTRUCTOR_ERROR(), hookDef.getClassName());
				return;
			}
			
			// It must implement the right interface.
			if (! (hookObj instanceof LifecycleHook<?>)) {
				parserResponse.addMessage(ServerMessageLocal.HOOK_CLASS_NO_INTERFACE(), hookObj.getClass().getName(), LifecycleHook.class.getName());
				return;
			}
			
			LifecycleHook<? extends LifecycleEvent> hookImpl = (LifecycleHook<LifecycleEvent>) hookObj;
			
			// The implementation's scope must be compatible with that of the definition:
			// 1. Test-scoped hookDef must define an implementation which listens to a test aware event.
			if (hookDef instanceof StateScopedHook && ! StateAwareLifecycleEvent.class.isAssignableFrom(hookImpl.getLifecycleEventClass())) {
				parserResponse.addMessage(
						ServerMessageLocal.HOOK_STATE_SCOPE_VIOLATION(), 
						((StateScopedHookImpl)hookDef).location.getPath(), hookImpl.getLifecycleEventClass().getName());				
			}
			// 2. State-scoped hookDef must define an implementation which listens to a state aware event.
			if (hookDef instanceof VariationScopedHook && ! VariationAwareLifecycleEvent.class.isAssignableFrom(hookImpl.getLifecycleEventClass())) {
				parserResponse.addMessage(
						ServerMessageLocal.HOOK_VARIATION_SCOPE_VIOLATION(), 
						((VariationScopedHookImpl)hookDef).location.getPath(), hookImpl.getLifecycleEventClass().getName());				
			}

						
			// AOK. Add to the appropriate hook list.
			HookListEntry hle = new HookListEntry(hookImpl.getLifecycleEventClass(), hookDef);

			if (hookDef instanceof MetaScopedHook) {
				schemaHooks.add(hle);
				if (LOG.isDebugEnabled()) 
					LOG.debug(String.format("Registered schema-scoped hook class [%s] at [%s]", 
							hookDef.getClass(), ((SchemaHookImpl)hookDef).location.getPath()));
			}
			else if (hookDef instanceof StateScopedHook) {
				stateHooks.add(hle);
				if (LOG.isDebugEnabled()) 
					LOG.debug(String.format("Registered state-scoped hook [%s] at [%s]", 
							hookDef.getClassName(), ((StateScopedHookImpl)hookDef).location.getPath()));
			}
			else if (hookDef instanceof VariationScopedHook) {
				testHooks.add(hle);
				if (LOG.isDebugEnabled()) 
					LOG.debug(String.format("Registered test-scoped hook [%s] at [%s]", 
							hookDef.getClassName(), ((VariationScopedHookImpl)hookDef).location.getPath()));
			}
		}
		catch (Exception e) {
			parserResponse.addMessage(ServerMessageLocal.OBJECT_INSTANTIATION_ERROR(), e, hookDef.getClassName(), e.getClass().getName());
		}

	}
	
	/**
	 * Post all hooks listening on a particular LSE.
	 * @param listenerClass
	 * @param hook
	 * @return the hook passed in as argument.
	 */
   @SuppressWarnings("unchecked")
   public LifecycleEvent.PostResult post(LifecycleEvent event) {
			   	   	   
	   // 1. Build the hook chain, i.e. all hooks eligible for posting, in order.
	   
	   ArrayList<HookListEntry> chain = new ArrayList<HookListEntry>();
	   
	   if (event instanceof VariationAwareLifecycleEvent) {

		   for (HookListEntry hle : testHooks) {
				
			   if (hle.lseClass.isAssignableFrom(event.getClass()) &&
					   ((VariationAwareLifecycleEvent) event).getVariation().equals(((VariationScopedHook)hle.hookDef).getVariation())) {
											
				   chain.add(hle);
				}				   
			}
	   }

	   if (event instanceof StateAwareLifecycleEvent) {
		   
		   for (HookListEntry hle : stateHooks) {
			   			   
			   // Only post subscribers to the event type and whose state matches.
			   if (hle.lseClass.isAssignableFrom(event.getClass()) &&
					   ((StateAwareLifecycleEvent) event).getState().equals(((StateScopedHook)hle.hookDef).getState())) {
				   
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
			   buff.append(((SchemaHookImpl) hle.hookDef).location.getPath());
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
			   Optional<? extends LifecycleEvent.PostResult> result = hook.post(event);
			   
			   if (LOG.isTraceEnabled())
				   LOG.trace("Posted user hook [" + ((SchemaHookImpl) hle.hookDef).location.getPath() + "] with [" + event + "]. Result: [" + result + "]");
						
			   // If user hook returned a result, clip the chain.
			   if (result.isPresent()) return result.get();
		
		   }

		   // Either empty chain, or none cared to return a result.
		   // If this is a server event, post the default hook.
		   if (event instanceof LifecycleEvent) {
			   LifecycleEvent sle = (LifecycleEvent) event;
			   return ((LifecycleHook<LifecycleEvent>) sle.getDefaultHook()).post(sle).get();
		   }
		   
		   return null;

		} catch (ServerException e) {
			throw e;
		
		} catch (Exception e) {
			LOG.error(ServerError.HOOK_UNHANDLED_EXCEPTION.asMessage(hookDef.getClassName(), e.getMessage()), e);
			throw ServerExceptionRemote$.MODULE$.apply(ServerError.HOOK_UNHANDLED_EXCEPTION, LifecycleHook.class.getName(), e.getMessage());
		}				

	}

}
