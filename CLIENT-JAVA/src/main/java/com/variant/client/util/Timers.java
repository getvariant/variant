package com.variant.client.util;

/**
 * Thread local timer.
 *
 */
public class Timers {
	
	public static ThreadLocal<StopWatch> remoteTimer = new ThreadLocal<StopWatch>() {
		@Override protected StopWatch initialValue() { return new StopWatch(); }
	};

	public static ThreadLocal<StopWatch> localTimer = new ThreadLocal<StopWatch>() {
		@Override protected StopWatch initialValue() { return new StopWatch(); }
	};

	public static ThreadLocal<Counter> remoteCallCounter = new ThreadLocal<Counter>() {
		@Override protected Counter initialValue() { return new Counter(); }
	};

	/**
	 */
	public static class StopWatch {
		
		private long runningTotal = 0;
		private long lastStart = -1;
						
		public StopWatch reset() {
			runningTotal = 0;
			lastStart = -1;
			return this;
		}
		
		public long stop() {
			if (lastStart > 0) {
				runningTotal += (System.currentTimeMillis() - lastStart);
				lastStart = -1;
			}
			return runningTotal;
		}
		
		public boolean isStopped() {
			return lastStart < 0;
		}
		
		public void start() {
			if (lastStart > 0) 
				throw new RuntimeException("The timer is already started");
			lastStart = System.currentTimeMillis();
		}
		
		public long value() {
			if (lastStart > 0) 
				throw new RuntimeException("Stop timer first");
			return runningTotal;
		}
	}
	
	/**
	 */
	public static class Counter {
			
		private int value = 0;
		
		public void increment() {
			value++;
		}
		
		public int value() {
			return value;
		}
		
		public void reset() {
			value = 0;
		}

	}

}
