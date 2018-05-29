package com.variant.client.lifecycle;

/**
 * The interface to be implemented by user provided life-cycle hooks.
 *
 * @param <E> The life-cycle event class of interest.
 * 
 * @since 0.9
 */
public interface LifecycleHook<E extends LifecycleEvent> {
	
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
	 * @see com.variant.client.lifecycle.LifecycleEvent
     * @since 0.9
	 */
	void post(E event) throws Exception;

}