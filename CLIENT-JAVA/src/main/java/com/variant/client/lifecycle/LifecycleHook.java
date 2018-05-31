package com.variant.client.lifecycle;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.core.lifecycle.LifecycleEvent;

/**
 * The interface to be implemented by a life-cycle hook, which wants to be posted by one or more life-cycle events.
 * Whenever Variant raises a life cycle event of (sub)type returned by {@link #getLifecycleEventClass()},
 * this hook is posted via the {@link #post(ClientLifecycleEvent)} method. 
 * <p>
 * <p>Life-cycle hooks are registered by either {@link Connection#addLifecycleHook(LifecycleHook)} or
 * {@link Session#addLifecycleHook(LifecycleHook)}. Whenever a life-cycle event is raised, eligible hooks 
 * are posted asynchronously. If multiple hooks are registered for a particular life-cycle event, 
 * connection-level hooks are posted before session-level hooks.
 * <p>
 * An implementation must provide at least one public constructor: 
 * <ol>
 * <li>If no state initialization is required, a nullary constructor is sufficient. 
 * <li>If you need to pass an initial state to the newly constructed hook object, you must provide a constructor
 * which takes the single argument of the type of type <a href="https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html" target="_blank">com.typesafe.config.Config</a>.
 * Variant will invoke this constructor and pass it the parsed value of the {@code 'init'} property.
 * </ol>
 *
 * @param <E> The life-cycle event class to post this hook.
 * 
 * @since 0.9
 */
public interface LifecycleHook<E extends ClientLifecycleEvent> {
	
	/**
	 * Implementation must tell the client what life-cycle event type(s) it wants to be posted on.
	 * If this method returns a super-type, this hook will be posted for it and all its descendant 
	 * event types.
	 * 
	 * @return A {@link java.lang.Class} object associated with the life-cycle event type(s) of interest.
     * @since 0.9
	 */
	Class<E> getLifecycleEventClass();
	
	/**
	 * The callback method, called by Variant client when a life cycle event of type assignable to that returned
	 * by {@link #getLifecycleEventClass()} is raised.
	 * 
	 * @param event The posting life cycle event. May be further examined for details.
	 * 
	 * @see com.variant.client.lifecycle.ClientLifecycleEvent
     * @since 0.9
	 */
	void post(E event) throws Exception;

}
