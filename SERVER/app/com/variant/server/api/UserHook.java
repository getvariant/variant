package com.variant.server.api;

import com.variant.core.LifecycleEvent;
import com.variant.core.schema.Hook;

/**
 * <p>The interface to be implemented by a user hook, which wants to be posted by a life cycle event.
 * Whenever Variant server reaches the execution point corresponding to the life cycle event
 * type assignable to the class returned by {@link #getLifecycleEventClass()}, this listener is posted by 
 * Variant server via the {@link #post(LifecycleEvent)} method.
 * 
 * <p>It is permissible to register multiple listeners for the same hook type.
 * In this case, Variant server will call them in the order of registration until {@link #post(LifecycleEvent)}
 * returns a value other than null.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
 */

public interface UserHook<E extends LifecycleEvent> {

	/**
	 * Implementation must tell the server what life cycle event type(s) it wants to be posted on.
	 * If this method returns a super-type, this hook will be posted for all descendant 
	 * event types.
	 * 
	 * @return A {@link java.lang.Class} object associated with the life cycle event type(s) of interest.
     * @since 0.5
	 */
	public Class<E> getLifecycleEventClass();
	
	/**
	 * The callback method called by the server when a life cycle event of type assignable to that returned
	 * by {@link #getLifecycleEventClass()} is reached.
	 * 
	 * @param event The posting event. May be further examined for details of the posting life cycle event.
	 * @param hook The schema hook corresponding to this object. This may be useful when multiple hooks are posted
	 *             with the same life cycle event.
	 * 
     * @since 0.7
	 */
	public void post(E event, Hook hook);

}