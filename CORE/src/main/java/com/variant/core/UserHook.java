package com.variant.core;

import com.typesafe.config.Config;
import com.variant.core.schema.Hook;

/**
 * <p>The interface to be implemented by a user hook, which wants to be posted of a life cycle event.
 * Whenever Variant server reaches the execution point corresponding to the life cycle event
 * type assignable to the class returned by {@link #getLifecycleEventClass()}, this listener is posted by 
 * Variant server via the {@link #post(LifecycleEvent)} method.
 * 
 * <p>It is permissible to register multiple hooks for the same life cycle event type.
 * In this case they form a hook chain, and Variant server will call them in the order of registration.
 * Hooks are posted until the {@link #post(LifecycleEvent)} method returns a non-null value. If
 * none of user defined hooks returned a non-null value, the default hook is posted, which is guaranteed
 * to return a value.
 * 
 * <p>An implementation must provide a public no-argument constructor. If it is desirable to initialize a
 * newly constructed object, use {@link #init(Config, Hook)}.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
 */

public interface UserHook<E extends LifecycleEvent> {

	/**
	 * <p>Object initializer. By contract, an implementation must provide a puboic, no-argument constructor, which Variant server
	 * will use to instantiate it. However, user may wish to pass initialization data into this newly instantiated object by
	 * providing the <code>init</code> parameter in the schema definition of the hook. Its value can be an arbitrary JSON literal
	 * which will be parsed and passed to this method in the form of a <a href="https://typesafehub.github.io/config/latest/api/">
	 * Typesafe Config</a> object, rooted at the key <code>init</init>.
	 * 
	 * @param config The configuration data as instance of <a href="https://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html">
	 * Config</a> type.
	 * @param hook The schema definition of this hook. May be useful when multiple hooks are posted by the same life cycle event.
	 *             
    * @since 0.7
	 */
	public void init(Config config, Hook hook) throws Exception;
		
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