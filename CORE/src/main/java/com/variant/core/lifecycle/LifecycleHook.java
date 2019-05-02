package com.variant.core.lifecycle;

import java.util.function.Supplier;

/**
 * The interface to be implemented by a server life-cycle hook, which wants to be posted by one or more life-cycle events.
 * Whenever Variant raises a life cycle event of (sub)type returned by {@link #getLifecycleEventClass()},
 * this hook is posted via the {@link #post(LifecycleEvent)} method. 
 * <p>
 * <p>Life-cycle hooks are defined in the experiment schema at the meta, state or test level. Those hooks, defined
 * at the meta level are <em>schema-scoped</em> and are applicable to all states and tests in the schema.
 * Hooks defined at the state level are <em>state-scoped</em> and are only applicable to the state where
 * they are defined. Finally, hooks defined at the test level are <em>test-scoped</em> and are only applicable
 * to the test where they are defined. It is an error to define a state-scoped hook which listens to a non-
 * {@link StateAwareLifecycleEvent}. Likewise, is an error to define a test-scoped hook which listens to a non-
 * {@link VariationAwareLifecycleEvent}. Whenever a life-cycle event of type T is raised, Variant server posts 
 * all hooks whose {@link #getLifecycleEventClass()} returns either T or its subclass.
 * <p>
 * If multiple hooks are to be posted by same life-cycle event, they form a hook chain, and Variant server will 
 * post them in the order they were defined in the schema. Hooks are posted one after another, until the 
 * {@link #post(LifecycleEvent)} method returns a non-null value. If no user defined hook returned a non-null value,
 * the default hook is posted.
 * <p>
 * An implementation must provide at least one public constructor: 
 * <ol>
 * <li>If no state initialization is required, a nullary constructor is sufficient. 
 * <li>If you need to pass an initial state to the newly constructed hook object, you must provide a constructor
 * which takes the single argument of type <a href="https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html" target="_blank">com.typesafe.config.Config</a>.
 * Variant will invoke this constructor and pass it the parsed value of the {@code 'init'} property.
 * </ol>
 * 
 * <p>Variant creates a new instance of the implementing class for each raised event.
 * 
 * @param <E> The life-cycle event class to post this hook.
 *
 * @since 0.5
 *
 */

public interface LifecycleHook<E extends LifecycleEvent> {
		
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
	 * The result of the {@link LifecycleHook#post(LifecycleEvent)} callback. Concrete implementations will have methods
	 * for the user code to set the details of the outcome of the post operation expected by the server. Call the
	 * appropriate method of {@code com.variant.server.api.lifecycle.PostResultFactory} to obtain a post result to be
	 * returned by your code.
	 * 
	 * @since 0.7
	 */
	public interface PostResult {}
	
}