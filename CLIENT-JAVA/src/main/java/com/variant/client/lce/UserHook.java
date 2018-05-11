package com.variant.client.lce;

/**
 * 
 *
 * @param <E>
 */
public interface UserHook<E extends LifecycleEvent> {
	
	/**
	 * Implementation must tell the server what life cycle event type(s) it wants to be posted on.
	 * If this method returns a super-type, this hook will be posted for it and all its descendant 
	 * event types.
	 * 
	 * @return A {@link java.lang.Class} object associated with the life cycle event type(s) of interest.
     * @since 0.5
	 */
	Class<E> getLifecycleEventClass();
	
	/**
	 * The callback method, called by the server when a life cycle event of type assignable to that returned
	 * by {@link #getLifecycleEventClass()} is reached. This method may either return a usable result
	 * (in the form of a {@link PostResult} object, or pass the event down the hook chain, i.e. let another 
	 * hook process the event. 
	 * 
	 * @param event The posting life cycle event. May be further examined for details, e.g. the foreground session
	 *              for the runtime event types.
	 * @return An object of type {@link PostResult} or null. An "empty" {@link PostResult} object is obtained by
	 *              calling the appropriate factory method in {@link com.variant.server.api.hook.PostResultFactory}.
	 * 
	 * @see com.variant.server.api.hook.PostResultFactory
    * @since 0.7
	 */
	void post(E event) throws Exception;

}