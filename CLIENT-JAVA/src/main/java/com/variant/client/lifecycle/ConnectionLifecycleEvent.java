package com.variant.client.lifecycle;

import com.variant.client.Connection;

/**
 * 
 *
 */
public interface ConnectionLifecycleEvent extends LifecycleEvent {

	/**
	 * 
	 */
	Connection getConnection();
	
}
