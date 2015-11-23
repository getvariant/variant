package com.variant.core.session;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.parser.MessageTemplate;

public class SessionIdTrackerFactory {

	/**
	 * 
	 * @return
	 */
	public static VariantSessionIdTracker getInstance(String className) {
		
		try {
			Class<?> persisterClass = Class.forName(className);
			Object persisterObject = persisterClass.newInstance();
			if (persisterObject instanceof VariantSessionIdTracker) {
				return (VariantSessionIdTracker) persisterObject;
			}
			else {
				throw new VariantBootstrapException(MessageTemplate.BOOT_SID_TRACKER_NO_INTERFACE, className, VariantSessionIdTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate session ID tracker class [" + className + "]", e);
		}
	}
}
