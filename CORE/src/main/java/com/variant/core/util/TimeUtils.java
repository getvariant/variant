package com.variant.core.util;
/**
 * Replacement for apache commons lang3 time utils.
 * @author Igor
 *
 */
public class TimeUtils {

	public final static long MILLIS_PER_SECOND =     1000L;
	public final static long MILLIS_PER_MINUTE =    60000L;
	public final static long MILLIS_PER_HOUR   =  3600000L;
	public final static long MILLIS_PER_DAY    = 86400000L;
	
	/**
	 * Format a time duration expressed in milliseconds as hh:mm:ss.SSS
	 * @param duration
	 * @return
	 */
	public static String formatDuration(long durationMillis) {
		
		int hours = (int) (durationMillis / MILLIS_PER_HOUR);
		int remainder = (int) (durationMillis % MILLIS_PER_HOUR);
		int minutes = remainder / (int) MILLIS_PER_MINUTE;
		remainder = remainder % (int) MILLIS_PER_MINUTE;
		int seconds = remainder / (int) MILLIS_PER_SECOND;
		int millis = remainder % (int) MILLIS_PER_SECOND;

		//System.out.println("* " + hours + " * " + minutes + " * " + seconds + " * " + millis);
		
		StringBuilder result = new StringBuilder();
		if (hours > 0) result.append(hours).append(':');
		
		if (minutes > 9) result.append(minutes);
		else if (minutes > 0) result.append('0').append(minutes);
		else result.append("00");
		result.append(':');
		
		if (seconds > 9) result.append(seconds);
		else if (seconds > 0) result.append('0').append(seconds);
		else result.append("00");
		result.append('.');
		
		if (millis > 99) result.append(millis);
		else if (millis > 9) result.append('0').append(millis);
		else if (millis > 0) result.append("00").append(millis);
		else result.append("000");

		
		return result.toString();
	}
	
	/**
	 * Testing
	 * @param args
	 */
	public static void main(String[] args) {
		long millis[] = {
				0, 1, 9, 10, 99, 100, 999, 1000, 1001, 
				MILLIS_PER_MINUTE, 9 * MILLIS_PER_MINUTE - 1, 59 * MILLIS_PER_MINUTE + 1,  59 * MILLIS_PER_MINUTE + 59000,
				60 * MILLIS_PER_MINUTE -1, 60 * MILLIS_PER_MINUTE +1,
				MILLIS_PER_HOUR, MILLIS_PER_HOUR + MILLIS_PER_MINUTE, MILLIS_PER_HOUR + 9 * MILLIS_PER_MINUTE - 1, 
				MILLIS_PER_HOUR + 59 * MILLIS_PER_MINUTE + 1, MILLIS_PER_HOUR + 59 * MILLIS_PER_MINUTE + 59000,
				MILLIS_PER_HOUR + 60 * MILLIS_PER_MINUTE -1, MILLIS_PER_HOUR + 60 * MILLIS_PER_MINUTE +1,
				MILLIS_PER_DAY
		};
		
		for (long m: millis) System.out.println(m + " = " + formatDuration(m));
	}
}
