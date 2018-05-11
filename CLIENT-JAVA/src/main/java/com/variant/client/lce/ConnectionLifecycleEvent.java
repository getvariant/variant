package com.variant.client.lce;

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
