package com.variant.core.flashpoint;

public interface FlashpointListener <F extends Flashpoint> {

	/**
	 * What flashpoint type this listener is interested in being posted of?
	 * @return
	 */
	public Class<F> getFlashpointClass();
	
	/**
	 * Post this listener with a particular flashpoint.
	 * @param flashpoint
	 */
	public void post(F flashpoint);

}