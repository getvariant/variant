package com.variant.client.lifecycle;

import com.variant.client.Connection;

/**
 * Connection-scoped life-cycle event.
 *
 * @since 0.9
 */
public interface ConnectionLifecycleEvent extends LifecycleEvent {

	/**
	 * Get connection associated with this connection-scoped life-cycle event.
	 */
	Connection getConnection();
	
}
