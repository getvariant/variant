package com.variant.client.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.TargetingTracker;
import com.variant.core.util.Tuples.Tripple;

/**
 * Targeting tracker trait that knows how to marshal a collection of experience entries
 * to and from string.  Each experience entry also has a timestamp. Entries in the string 
 * are organized as follows:
 * 1234567:test:expereince|1234567:test:experience ...
 * 
 * @author Igor
 *
 */
public abstract class TargetingTrackerString implements TargetingTracker {

	private static Logger LOG = LoggerFactory.getLogger(TargetingTrackerString.class);
	
	protected TargetingTrackerString() {}
		
	/**
	 * Parse the content from an input string.
	 * @param input
	 * @param ssn
	 */
	public Set<Entry> fromString(String input) {
				
		Set<Entry> result = new HashSet<Entry>();
		
		if (input == null || input.length() == 0) return result;
		
		for (String entry: input.split("\\|")) {
			
			if (entry != null) {
				
				entry = entry.trim();				
				if (entry.length() == 0) continue;
				
				String[] tokens = entry.split("\\.");
				if (tokens.length == 3) {
					
					result.add(new TargetingTrackerEntryImpl(Long.parseLong(tokens[0]), tokens[1], tokens[2]));

					/**** !!! ALL THIS MUST GO ELSEWHERE !!! *****
					try {
						Test test = getSession().getSchema().getTest(tokens[1]);
						if (test == null) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Ignored non-existent test [" + tokens[1] + "]");
							}
							continue;
						}
						Experience experience = test.getExperience(tokens[2]);
						if (experience == null) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Ignored non-existent experience [" + tokens[1] + "." + tokens[2] + "]");
							}
							continue;
						}
						
						long timestamp = Long.parseLong(tokens[0]);
						// ignore if user hasn't seen this experience for TEST_MAX_IDLE_DAYS_TO_TARGET days,
						// unless it is set to 0, which means we honor it for life of experiment. 
						int idleDaysToTarget = getSession().getConfig().getInt(ConfigKeys.TARGETING_STABILITY_DAYS);
						if (idleDaysToTarget > 0 && System.currentTimeMillis() - timestamp > idleDaysToTarget * TimeUtils.MILLIS_PER_DAY) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Ignored idle experience [" + tokens[1] + "." + tokens[2] + "]");
							}
							continue;
						}
						
						result.add(new TargetingTrackerEntryImpl(experience, timestamp, getSession()));
					}
					catch (Exception e) {
						// any parsing error is due to end user's mucking with the string...
						if (LOG.isDebugEnabled()) {
							LOG.debug("Unable to parse entry [" + entry + "]", e);
						}
					}
					*******/
				}
				else {
					if (LOG.isInfoEnabled()) {
						LOG.info("Unable to parse entry [" + entry + "]");
					}					
				}
			}
		}
		
		return result;
	}
	
	/**
	 * From entires.
	 */
	public static String toString(Collection<Entry> entries) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Entry e: entries) {
			if (first) first = false;
			else sb.append("|");
			sb.append(e);
		}
		return sb.toString();
	}

	/**
	 * From tripples.
	 */
	@SafeVarargs
	public static String toString(Tripple<Long,String,String>...entries) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Tripple<Long,String,String> entry: entries) {
			if (first) first = false;
			else sb.append("|");
			sb.append(entry.arg1()).append('.').append(entry.arg2()).append('.').append(entry.arg3());
		}
		return sb.toString();
	}

}
