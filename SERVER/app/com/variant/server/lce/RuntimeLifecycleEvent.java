package com.variant.server.lce;

import com.variant.core.lce.LifecycleEvent;
import com.variant.server.api.Session;

/**
 * Super-interface for all life cycle event types, which are triggered at experiment's runtime.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface RuntimeLifecycleEvent extends LifecycleEvent {
	
	/**
	 * User session on whose behalf the event is triggered
	 * @return
	 * @since 0.8
	 */
	Session getSession();
}
