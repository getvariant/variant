package com.variant.core.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.apache.commons.collections4.Predicate;

import com.variant.core.schema.Schema;
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
		
		private String testName;
		private String experienceName;
		private long timestamp;
		
		private Entry(String testName, String experienceName, long timestamp) {
			this.testName = testName;
			this.experienceName = experienceName;
			this.timestamp = timestamp;
		}
		
		public String getTestName() {return testName;}				
		public String getExperienceName() {return experienceName;}		
		public long getTimestamp() {return timestamp;}		
		
		/**
		 */
		public String toString() {
			return testName + "." + experienceName + "." + timestamp;
		}

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
	public int size() {
		return entryMap.size();
	}
		
	/**
	 * 
	 * @return
	 */
	public Collection<Entry> getAll(Predicate<Entry> filter) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		for (Entry entry: entryMap.values()) {
			if (filter.evaluate(entry)) result.add(entry);
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * No predicate.
	 * @return
	 */
	public Collection<Entry> getAll() {
		return getAll(
			new Predicate<Entry>() {
				@Override public boolean evaluate(Entry object) { return true;}
			}
		);
	}

	/**
	 * Get all as experiences, looked up in caller-provided schema.
	 * @param schema
	 * @return
	 */
	public Collection<Experience> getAllAsExperiences(Schema schema) {
		ArrayList<Experience> result = new ArrayList<Experience>();
		for (Entry entry: entryMap.values()) {
			Test test = schema.getTest(entry.testName);
			if (test != null) {
				Experience experience = test.getExperience(entry.experienceName);
				if (experience != null) result.add(experience);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	public Entry get(String testName) {
		return entryMap.get(testName);
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	public Experience getAsExperience(String testName, Schema schema) {
		Entry entry =  entryMap.get(testName);
		if (entry != null) {
			return schema.getTest(entry.testName).getExperience(entry.experienceName);
		}
		return null;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public Entry remove(String testName) {
		return entryMap.remove(testName);
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public Entry add(String testName, String experienceName, long timestamp) {
		return entryMap.put(testName, new Entry(testName, experienceName, timestamp));
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public Entry add(Experience experience) {
		String testName = experience.getTest().getName();
		return entryMap.put(testName, new Entry(testName, experience.getName(), System.currentTimeMillis()));
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public void touch(String testName) {
		Entry entry = entryMap.remove(testName);
		if (entry != null) entry.timestamp = System.currentTimeMillis();
	}
	
}