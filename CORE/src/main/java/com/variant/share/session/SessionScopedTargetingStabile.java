package com.variant.share.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.variant.share.schema.Schema;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;

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
	public Set<Experience> getAllAsExperiences(Schema schema) {
		LinkedHashSet<Experience> result = new LinkedHashSet<Experience>();
		for (Entry entry: entryMap.values()) {
			Variation test = schema.getVariation(entry.testName).get();
			if (test != null) {
				Experience experience = test.getExperience(entry.experienceName).get();
				if (experience != null) result.add(experience);
			}
		}
		return Collections.unmodifiableSet(result);
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
		return entry == null ? null :
			schema.getVariation(entry.testName).get().getExperience(entry.experienceName).get();
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
		String tname = experience.getVariation().getName();
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
