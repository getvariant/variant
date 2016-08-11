package com.variant.core.hook;

/**
 * <p>The interface to be implemented by a user hook listener, which wants to be posted by a user hook.
 * Host code passes a custom implementation of this to <code>VariantClient.addHookListener(HookListener)</code>
 * in order to be posted of a user hook. Whenever Variant reaches the execution point corresponding to the hook
 * type assignable to the class returned by {@link #getHookClass()}, this listener is posted by Variant server
 * via the {@link #post(UserHook)} method.
 * 
 * <p>It is permissible to register multiple listeners for the same hook type.
 * In this case, the container will call them sequentially, in the order of registration.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
 */

public interface HookListener <H extends UserHook> {

	/**
	 * Implementation must tell the server what user hook type(s) it wants to be posted on.
	 * If this method returns a super-type, this listener will be posted for all descendant 
	 * hook types.
	 * 
	 * @return A {@link java.lang.Class} object associated with the hooks(s) of interest.
     * @since 0.5
	 */
	public Class<H> getHookClass();
	
	/**
	 * Variant server calls this to post this listener with a concrete hook.
	 * 
	 * @param hook A concrete user hook that is of the type returned by {@link #getHookClass()}
	 *                   or its sub-type.
     * @since 0.5
	 */
	public void post(H hook);

}