package com.variant.client.util;

/**
 * Thread local timer.
 *
 */
public class Timers {
	
	public static ThreadLocal<StopWatchWithCounter> remoteTimer = new ThreadLocal<StopWatchWithCounter>() {
		@Override protected StopWatchWithCounter initialValue() { return new StopWatchWithCounter(); }
	};

	public static ThreadLocal<StopWatch> localTimer = new ThreadLocal<StopWatch>() {
		@Override protected StopWatch initialValue() { return new StopWatch(); }
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
		
		public void increment(long incr) {
			if (lastStart > 0) 
				throw new RuntimeException("Cannot increment a running timer");
			runningTotal += incr;
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
		
		/**
		 * Clear after reading to avoid stale state.
		 * @return
		 */
		public long getAndClear() {
			if (lastStart > 0) 
				throw new RuntimeException("Stop timer first");
			long result = runningTotal;
			runningTotal = 0;
			return result;
		}
	}
	
	/**
	 */
	public static class StopWatchWithCounter extends StopWatch {
			
		private int count = 0;
		
		public void increment() {
			count++;
		}
		
		/**
		 * Clear after reading to avoid stale state.
		 * @return
		 */
		public int getAndClearCount() {
			int result = count;
			count = 0;
			return result;
		}
		
		@Override
		public StopWatchWithCounter reset() {
			super.reset();
			count = 0;
			return this;
		}

	}

}
