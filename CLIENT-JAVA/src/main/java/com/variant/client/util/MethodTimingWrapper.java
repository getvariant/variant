package com.variant.client.util;

import static com.variant.client.impl.ConfigKeys.SYS_PROP_TIMERS;

/**
 * Execute a timed operation
 *
 * @param <T>
 */
public class MethodTimingWrapper<T> {

	public T exec(Operation<T> op) {
			
		if (System.getProperty(SYS_PROP_TIMERS) != null) {
			Timers.localTimer.get().reset().start();
			Timers.remoteTimer.get().reset();
		}
		
		T result = op.apply();
		
		if (System.getProperty(SYS_PROP_TIMERS) != null) {
			Timers.localTimer.get().stop();
		}

		return result;
	}
	
	public static interface Operation<V> {
		V apply();
	}
}
