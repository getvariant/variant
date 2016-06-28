package com.variant.core.hook;

/**
 * <p>Client code implements this in order to subscribe to a user hook by calling
 * {@link com.variant.core.Variant#addHookListener(HookListener)}.
 * Whenever a particular hook is reached, this listener is posted by the container
 * via the {@link #post(UserHook)} method.
 * 
 * <p>It is permissible to register multiple listeners for the same hook type.
 * In this case, the container will call them sequentially, in the order they had been
 * registered.
 * 
 * @see com.variant.core.Variant#addHookListener(HookListener)
 * @author Igor Urisman
 * @since 0.5
 *
 */

public interface HookListener <H extends UserHook> {

	/**
	 * Implementation must tell the server what user hook type(s) it wants to listen for.
	 * If this method returns a super-type, this listener will be posted with each descendant 
	 * type hook.
	 * 
	 * @return A {@link java.lang.Class} object associated with the hooks(s) of interest.
     * @since 0.5
	 */
	public Class<H> getHookClass();
	
	/**
	 * The container calls this to post this listener with a concrete hook.
	 * 
	 * @param hook A concrete user hook that is of the type returned by {@link #getHookClass()}
	 *                   or its sub-type.
     * @since 0.5
	 */
	public void post(H hook);

}