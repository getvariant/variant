package com.variant.client.lifecycle;

import com.variant.client.Session;

/**
 * Session-scoped life-cycle event.
 *
 * @since 0.9
 */
public interface SessionLifecycleEvent extends LifecycleEvent {

	/**
	 * Get session associated with this session-scoped life-cycle event.
	 */
	Session getSession();
	
}
