package com.variant.core.flashpoint;

import java.util.ArrayList;

/**
 * Flashpoint processor
 * 
 * @author
 *
 */
public class Flasher {

	private ArrayList<FlashpointListener<? extends Flashpoint>> listeners = 
			new ArrayList<FlashpointListener<? extends Flashpoint>>();

	/**
	 * 
	 */
	public Flasher() {}
	
	/**
	 * 
	 * @param listener
	 */
	public void addListener(FlashpointListener<? extends Flashpoint> listener) {
		listeners.add(listener);
	}
	
	/**
	 * 
	 */
	public void clear() {
		listeners.clear();
	}

	/**
	 * Post all listeners listening on a particular flashpoint.
	 * @param listenerClass
	 * @param flashpoint
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void post(Flashpoint flashpoint) {
		for (FlashpointListener listener: listeners) {
			if (listener.getFlashpointClass().isInstance(flashpoint)) 
				listener.post(flashpoint);
		}
	}
}

