package com.variant.core;

import com.typesafe.config.Config;
import com.variant.core.lce.LifecycleEvent;
import com.variant.core.lce.StateAwareLifecycleEvent;
import com.variant.core.lce.TestAwareLifecycleEvent;

/**
 * <p>The interface to be implemented by a user hook, which wants to be posted of a life cycle event.
 * Whenever Variant triggers a life cycle event type assignable to the class returned by {@link #getLifecycleEventClass()},
 * this listener is posted via the {@link #post(LifecycleEvent)} method.
 * 
 * <p>It is permissible to register multiple hooks for the same life cycle event type.
 * In this case they form a hook chain, and Variant server will call them in the order of registration.
 * Hooks are posted until the {@link #post(LifecycleEvent)} method returns a non-null value. If
 * none of user defined hooks returned a non-null value, the default hook is posted, which is guaranteed
 * to return a value.
 * 
 * <p>User hooks are defined in the experiment schema at the meta, state or test level. Those hooks, defined
 * at the meta level are <em>schema-scoped</em> and are applicable to all states and tests in the schema.
 * Hooks defined at the state level are <em>state-scoped</em> and are only applicable to the state where
 * they are defined. Finally, hooks defined at the test level are <em>test-scoped</em> and are only applicable
 * to the test where they are defined. It is an error to define a state-scoped hook which listens to a non-
 * {@link StateAwareLifecycleEvent}. Likewise, is an error to define a test-scoped hook which listens to a non-
 * {@link TestAwareLifecycleEvent}.
 * 
 * <p>An implementation must provide at least one public constructor: 
 * <ol>
 * <li>If no state initialization is required, the default nullary constructor is sufficient. 
 * <li>If you need to pass an initial state to the newly constructed hook object, you must provide a constructor
 * which takes the single argument of the type {@link Config}. Variant will invoke this constructor and pass it
 * the value of the hook definitions's {@code init} property, parsed and rooted at element {@code 'init'}.
 * </ol>
 * 
 * <p>Variant creates a new instance of the implementation class for each triggered event.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
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
	public Class<E> getLifecycleEventClass();
	
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
	public PostResult post(E event) throws Exception;

	
	/**
	 * <p>The result of the {@link UserHook#post(LifecycleEvent)} callback. Concrete implementations will have methods
	 * for the user code to set the details of the outcome of the post operation expected by the server.
	 * 
	 * <p>T  
	 * 
	 * @since 0.7
	 */
	public interface PostResult {}
}