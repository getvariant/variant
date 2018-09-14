package com.variant.core.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.util.Predicate;

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
		
		/**
		 * Parse from the string prouce by the toString() method.
		 * @param string
		 * @return
		 */
		public static Entry parse(String string) {
			String[] tokens = string.split("\\.");
			return new Entry(tokens[0], tokens[1], Long.parseLong(tokens[2]));
		}
		
		/**
		 * 
		 */
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
			if (filter.test(entry)) result.add(entry);
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
				@Override public boolean test(Entry object) { return true;}
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
	public Entry add(Entry entry) {
		return entryMap.put(entry.testName, entry);
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
	public Entry add(Experience experience, long timestamp) {
		String tname = experience.getTest().getName();
		String ename = experience.getName();
		return entryMap.put(tname, new Entry(tname, ename, timestamp));
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	public Entry add(Experience experience) {
		return add(experience, System.currentTimeMillis());
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
