package com.variant.client.lifecycle;

import com.variant.client.Connection;

/**
 * Connection-aware life-cycle event.
 *
 * @since 0.9
 */
public interface ConnectionAwareLifecycleEvent extends ClientLifecycleEvent {

	/**
	 * Get connection associated with this connection-scoped life-cycle event.
	 */
	Connection getConnection();
	
}
