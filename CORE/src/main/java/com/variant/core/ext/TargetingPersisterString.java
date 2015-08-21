package com.variant.core.ext;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.TargetingPersisterSupport;

/**
 * Emulates targeting persister that is tracked by a cookie,
 * i.e. in an off-board string.
 * 
 * Entries in the string are organized as follows:
 * 1234567:test:expereince|1234567:test:experience ...
 * 
 * @author Igor
 *
 */
public class TargetingPersisterString extends TargetingPersisterSupport {

	private Logger LOG = LoggerFactory.getLogger(TargetingPersisterString.class);
		
	/**
	 * Parse the content from an input string.
	 * @param input
	 * @param ssn
	 */
	private void parseFromString(String input, VariantSession ssn) {
				
		for (String entry: input.split("\\|")) {
			
			if (entry != null) {
				
				entry = entry.trim();				
				if (entry.length() == 0) continue;
				
				String[] tokens = entry.split("\\.");
				if (tokens.length == 3) {
					try {
						Test test = Variant.Factory.getInstance().getSchema().getTest(tokens[1]);
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
						// ignore if user hasn't seen this experience for idleDaysToLive days,
						// inless idleDaysToLive is 0, which means forever. 
						if (test.getIdleDaysToLive() > 0 && System.currentTimeMillis() - timestamp > test.getIdleDaysToLive() * DateUtils.MILLIS_PER_DAY) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Ignored idle experience [" + tokens[1] + "." + tokens[2] + "]");
							}
							continue;
						}
						
						add(experience, timestamp);
					}
					catch (Exception e) {
						// any parsing error is due to end user's mucking with the string...
						if (LOG.isDebugEnabled()) {
							LOG.debug("Unable to parse entry [" + entry + "] for session [" + ssn.getId() + "]", e);
						}
					}
				}
				else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Unable to parse entry [" + entry + "] for session [" + ssn.getId() + "]");
					}					
				}
			}
		}
	}
	
	@Override
	public void persist(Object userData) {
		// Nothing to do - in memory only.
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public void initialized(VariantSession ssn, Object userData) { 
		parseFromString((String) userData, ssn);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Entry e: entryMap.values()) {
			if (first) first = false;
			else sb.append("|");
			sb.append(e.getTimestamp()).append(".").append(e.getExperience());
		}
		return sb.toString();
	}
	
}
