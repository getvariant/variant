package com.variant.core.session;


public class SessionIdTrackerFactory {

	/**
	 * 
	 * @return
	 *
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
	*/
}
