package com.variant.client.lifecycle;

import com.variant.client.Session;

/**
 * Session-aware life-cycle event.
 *
 * @since 0.9
 */
public interface SessionAwareLifecycleEvent extends ClientLifecycleEvent {

	/**
	 * Get session associated with this session-scoped life-cycle event.
	 */
	Session getSession();
	
}
