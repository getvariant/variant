package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Session scoped targeting stabilizer provides session scoped targeting stability.
 * Manipulates targeted experiences in a linked list and lives in session.
 * 
 * @author Igor
 *
 */
public class SessionScopedTargetingStabile {


	// Entries are held in a map keyed by test name
	protected LinkedHashMap<String, Entry> entryMap = new LinkedHashMap<String, Entry>();
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	
	public static class Entry {
		
		private Experience experience;
		private long timestamp;
		
		private Entry(Experience experience, long timestamp) { 
			this.experience = experience;
			this.timestamp = timestamp;
		}
		
		/**
		 */
		public Experience getExperience() {return experience;}
		
		/**
		 */
		public long getTimestamp() {return timestamp;}		
	}
	
	/**
	 * Constructor initializes the state with an empty entry list.
	 * 
	 * @param experiences
	 * @param timestamp
	 */
	public SessionScopedTargetingStabile() {}

	/**
	 * 
	 * @return
	 */
	public Collection<Experience> getAll() {
		ArrayList<Experience> result = new ArrayList<Test.Experience>();
		for (Entry entry: entryMap.values()) result.add(entry.experience);
		return Collections.unmodifiableList(result);
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	public Experience get(Test test) {
		Entry result = entryMap.get(test.getName());
		return result == null ? null : result.experience;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public Experience remove(Test test) {
		Entry result = entryMap.remove(test.getName());
		return result == null ? null : result.experience;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public Experience add(Experience experience, long timestamp) {
		Entry result = entryMap.put(experience.getTest().getName(), new Entry(experience, timestamp));
		return result == null ? null : result.experience;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public void touch(Test test) {
		Entry entry = entryMap.remove(test.getName());
		if (entry != null) entry.timestamp = System.currentTimeMillis();
	}
	
}
