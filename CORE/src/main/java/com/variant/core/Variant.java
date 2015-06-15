package com.variant.core;

import com.variant.core.config.Config;

/**
 * The Variant Container.
 * 
 * @author Igor
 *
 */
public class Variant {

	private static Config config = null;
	
	/**
	 * Client code should not call this. 
	 */
	public static  void setConfig(Config config) {
		Variant.config = config;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Get current configuration.
	 * @return
	 */
	public static Config getConfig() {
	
		return config;
	
	}
	
}
