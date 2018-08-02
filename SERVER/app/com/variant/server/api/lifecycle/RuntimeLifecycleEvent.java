package com.variant.server.api.lifecycle;

import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.Session;


/**
 * <p>Super-type of all  life-cycle event types raised at run time. Concrete implementations
 * are made available to user code via the {@link LifecycleHook#post(LifecycleEvent)} callback.
 *
 * <p>Life-cycle hooks provide a consistent way of extending the functionality of Variant server 
 * and client with custom semantics via callback methods, which can augment default functionality
 * with application specific semantics.
 *  
 * @author Igor Urisman.
 * @since 0.8
 */

public interface RuntimeLifecycleEvent extends LifecycleEvent {
	
	/**
	 * User session on whose behalf the event is raised.
	 * @return
	 * @since 0.8
	 */
	Session getSession();

	/**
	 * The default life-cycle hook, which is automatically added to each life cycle event's hook chain.
	 * It is posted in case either 1) no life-cycle hooks were defined in the schema, or 2) none of those
	 * life-cycle hooks defined in the schema for this event type returned a non-null response.
	 * 
	 *  @since 0.7
	 */
	LifecycleHook<? extends RuntimeLifecycleEvent> getDefaultHook();
	
}