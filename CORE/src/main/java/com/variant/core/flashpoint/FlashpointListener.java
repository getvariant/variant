package com.variant.core.flashpoint;

/**
 * <p>Client code implements this in order to subscribe to a flashpoint by calling
 * {@link com.variant.core.Variant#addFlashpointListener(FlashpointListener)}.
 * Whenever a particular flashpoint is reached, this listener is posted by the container
 * via the {@link #post(Flashpoint)} method.
 * 
 * <p>It is permissible to register multiple listeners for the same flashpoint type.
 * In this case, the container will call them sequentially, in the order they had been
 * registered.
 * 
 * @see com.variant.core.Variant#addFlashpointListener(FlashpointListener)
 * @author Igor Urisman.
 * @since 0.5
 *
 */

public interface FlashpointListener <F extends Flashpoint> {

	/**
	 * Implementation must tell the container what concrete flashpoint type(s) it wants to listen for.
	 * If this method returns a super-type, this listener will be posted with each descendant flashpoint.
	 * 
	 * @return A {@link java.lang.Class} object associated with the flashpoint(s) of interest.
     * @since 0.5
	 */
	public Class<F> getFlashpointClass();
	
	/**
	 * The container calls this to post this listener with a concrete flashpoint.
	 * 
	 * @param flashpoint A concrete flashpoint that is of the type returned by {@link #getFlashpointClass()}
	 *                   or its sub-type.
     * @since 0.5
	 */
	public void post(F flashpoint);

}