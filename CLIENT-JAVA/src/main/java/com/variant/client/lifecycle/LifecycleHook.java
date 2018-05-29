package com.variant.client.lifecycle;

import com.typesafe.config.Config;
import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.StateAwareLifecycleEvent;
import com.variant.core.lifecycle.TestAwareLifecycleEvent;

/**
 * <p>The interface to be implemented by a life-cycle hook, which wants to be posted by a life-cycle event.
 * Whenever Variant raises a life cycle event type assignable to the class returned by {@link #getLifecycleEventClass()},
 * this hook is posted via the {@link #post(LifecycleEvent)} method.
 * 
 * <p>It is permissible to register multiple hooks for the same life cycle event type.
 * In this case they form a hook chain, and Variant server will call them in the order of registration.
 * Hooks are posted until the {@link #post(LifecycleEvent)} method returns a non-null value. If
 * none of user defined hooks returned a non-null value, the default hook is posted, which is guaranteed
 * to return a value.
 * 
 * <p>Life-cycle hooks are defined in the experiment schema at the meta, state or test level. Those hooks, defined
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
