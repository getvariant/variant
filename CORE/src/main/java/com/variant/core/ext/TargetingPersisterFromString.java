package com.variant.core.ext;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;

import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantSession;
import com.variant.core.impl.VariantCoreImpl;
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
public class TargetingPersisterFromString extends TargetingPersisterSupport {

	private Logger logger = ((VariantCoreImpl)Variant.Factory.getInstance()).getLogger();
		
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
							if (logger.isDebugEnabled()) {
								logger.debug("Ignored non-existent test [" + tokens[1] + "]");
							}
							continue;
						}
						Experience experience = test.getExperience(tokens[2]);
						if (experience == null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Ignored non-existent experience [" + tokens[1] + "." + tokens[2] + "]");
							}
							continue;
						}
						
						long timestamp = Long.parseLong(tokens[0]);
						// ignore if user hasn't seen this experience for idleDaysToLive days,
						// inless idleDaysToLive is 0, which means forever. 
						if (test.getIdleDaysToLive() > 0 && System.currentTimeMillis() - timestamp > test.getIdleDaysToLive() * DateUtils.MILLIS_PER_DAY) {
							if (logger.isDebugEnabled()) {
								logger.debug("Ignored idle experience [" + tokens[1] + "." + tokens[2] + "]");
							}
							continue;
						}
						
						add(experience, timestamp);
					}
					catch (Exception e) {
						// any parsing error is due to end user's mucking with the string...
						if (logger.isDebugEnabled()) {
							logger.debug("Unable to parse entry [" + entry + "] for session [" + ssn.getId() + "]", e);
						}
					}
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Unable to parse entry [" + entry + "] for session [" + ssn.getId() + "]");
					}					
				}
			}
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public void initialized(VariantSession ssn, UserData... userArgs) {

		if (userArgs.length != 1) 
			throw new VariantInternalException("Expected 1 user argument, but got [" + userArgs.length + "]");
 
		// first user data is the input string.  Parse it.
		String stringInput = ((UserDataFromString) userArgs[0]).stringInput;
		parseFromString(stringInput, ssn);
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
	
	/**
	 * 
	 */
	public static class UserDataFromString implements UserData {
		private String stringInput;
		public UserDataFromString(String stringInput) { this.stringInput = stringInput;}
	}
}
