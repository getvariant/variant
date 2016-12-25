package com.variant.core.util.inject;

import java.util.Map;

/**
 * An injectable type must extend this to guarantee uniform lifecycle method sinatures.
 * @author Igor
 */
public interface Injectable {

	/**
	 * Called by Injector, right after instantiation by Injector.
	 * @param core Core API in effect at the time of instantiation.
	 * @param initObject Parsed JSON from the configuration.
	 */
	public void init(Map<String, Object> initObject);
	
	/**
	 * Called by Injector?
	 */
	public void shutdown();

}
