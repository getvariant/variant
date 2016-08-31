package com.variant.client.session;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantClientPropertyKeys;
import com.variant.client.VariantTargetingTracker;
import com.variant.core.VariantProperties;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

/**
 * Targeting tracker trait that knows how to marshal a collection of experience entries
 * to and from string.  Each experience entry also has a timestamp. Entries in the string 
 * are organized as follows:
 * 1234567:test:expereince|1234567:test:experience ...
 * 
 * @author Igor
 *
 */
public abstract class TargetingTrackerString implements VariantTargetingTracker {

	private Logger LOG = LoggerFactory.getLogger(TargetingTrackerString.class);
	

	/**
	 * 
	 * @return
	 */
	abstract protected VariantProperties getProperties();
	
	/**
	 * Parse the content from an input string.
	 * @param input
	 * @param ssn
	 */
	public Collection<Entry> fromString(String input, Schema schema) {
				
		Collection<Entry> result = new ArrayList<Entry>();
		
		if (input == null || input.length() == 0) return result;
		
		for (String entry: input.split("\\|")) {
			
			if (entry != null) {
				
				entry = entry.trim();				
				if (entry.length() == 0) continue;
				
				String[] tokens = entry.split("\\.");
				if (tokens.length == 3) {
					try {
						Test test = schema.getTest(tokens[1]);
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
						int idleDaysToTarget = getProperties().get(VariantClientPropertyKeys.TEST_MAX_IDLE_DAYS_TO_TARGET, Integer.class);
						if (idleDaysToTarget > 0 && System.currentTimeMillis() - timestamp > idleDaysToTarget * DateUtils.MILLIS_PER_DAY) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Ignored idle experience [" + tokens[1] + "." + tokens[2] + "]");
							}
							continue;
						}
						
						result.add(new TargetingTrackerEntryImpl(experience, timestamp));
					}
					catch (Exception e) {
						// any parsing error is due to end user's mucking with the string...
						if (LOG.isDebugEnabled()) {
							LOG.debug("Unable to parse entry [" + entry + "]", e);
						}
					}
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
	 * 
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

}