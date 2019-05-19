package com.variant.core.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Random;

/**
 * Replacement for apache commons lang3 time utils.
 * @author Igor
 *
 */
public class TimeUtils {

	public final static int SECONDS_PER_MINUTE =  60;
	public final static int SECONDS_PER_HOUR   =  SECONDS_PER_MINUTE * 60;
	public final static int SECONDS_PER_DAY    = SECONDS_PER_HOUR * 24;
	
	/**
	 * Format a time duration expressed in milliseconds as hh:mm:ss.SSS
	 * @param duration
	 * @return
	 */
	public static String formatDuration(Duration duration) {
	
		long seconds = duration.get(ChronoUnit.SECONDS);
		long nanos = duration.get(ChronoUnit.NANOS);
		
		int dd = (int) (seconds / SECONDS_PER_DAY);
		int rem = (int) (seconds % SECONDS_PER_DAY);
		int hh = (int) (rem / SECONDS_PER_HOUR);
		rem = (int) (seconds % SECONDS_PER_HOUR);
		int mm = rem / SECONDS_PER_MINUTE;
		int ss = (int) (seconds % SECONDS_PER_MINUTE);

		//System.out.println("dd " + dd + " hh " + hh + " mm " + mm + " ss " + ss + " nanos " + nanos);
		
		StringBuilder result = new StringBuilder();
		if (dd > 0) result.append(dd).append("d ");
		if (hh > 0) result.append(String.format("%02d", hh)).append("h ");
		if (mm > 0) result.append(String.format("%02d", mm)).append("m ");
		result.append(String.format("%02d", ss)).append('.');
		// discard insignificant decimals.
		char[] decimals = String.format("%09d", nanos).toCharArray();
		int lastSignificantDigit = decimals.length;
		while (lastSignificantDigit > 0 && decimals[--lastSignificantDigit] == '0') {}			
		result.append(Arrays.copyOfRange(decimals, 0, lastSignificantDigit + 1)).append("s");

		return result.toString();
	}
	
	/**
	 * Testing
	 * @param args
	 */
	public static void main(String[] args) {
		
		Random rand  = new Random(System.currentTimeMillis());
		for (int i = 0; i<200; i++) {
			long n = Math.abs(rand.nextLong());
			Duration dur = Duration.ofNanos(n);
			System.out.println(formatDuration(dur));
		}
		
		long[] millis = {1999, 2000, 2001, 59999, 60000, 60001, 60999, 61000, 61001};
		Arrays.stream(millis).forEach(l -> System.out.println(formatDuration(Duration.ofMillis(l))));
	}
}
