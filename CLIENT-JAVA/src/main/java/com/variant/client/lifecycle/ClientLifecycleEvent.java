package com.variant.client.lifecycle;

import com.variant.client.ClientException;
import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;

/**
 * Super-interface for all client life-cycle Events.
 *
 */
public interface ClientLifecycleEvent extends LifecycleEvent {
	
	/**
	 * Default hooks are not supported on the client.
	 */
	default LifecycleHook<? extends LifecycleEvent> getDefaultHook() {
		throw new ClientException.Internal("Unsupported Operation");
	}

}
