package com.variant.core.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Basic implementation of a targeting persister as a list in memory.
 * Implements basic operations only.
 * 
 * @author Igor
 *
 */
abstract public class TargetingPersisterSupport implements TargetingPersister {

	/**
	 * 
	 */
	protected static class Entry {
		
		private Experience experience;
		private long timestamp;
		
		private Entry(Experience experience, long timestamp) { 
			this.experience = experience;
			this.timestamp = timestamp;
		}
		
		protected Experience getExperience() {return experience;}
		protected long getTimestamp() {return timestamp;}
	}

	// Experiences are held in a map keyed by test name
	protected LinkedHashMap<String, Entry> entryMap = new LinkedHashMap<String, Entry>();
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	@Override
	public Collection<Experience> getAll() {
		ArrayList<Experience> result = new ArrayList<Test.Experience>();
		for (Entry entry: entryMap.values()) result.add(entry.experience);
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	@Override
	public Experience get(Test test) {
		Entry result = entryMap.get(test.getName());
		return result == null ? null : result.experience;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	@Override
	public Experience remove(Test test) {
		Entry result = entryMap.remove(test.getName());
		return result == null ? null : result.experience;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	@Override
	public Experience add(Experience experience, long timestamp) {
		Entry result = entryMap.put(experience.getTest().getName(), new Entry(experience, timestamp));
		return result == null ? null : result.experience;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	@Override
	public void touch(Test test) {
		Entry entry = entryMap.remove(test.getName());
		if (entry != null) entry.timestamp = System.currentTimeMillis();
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public void addAll(Collection<Experience> experiences, long timestamp) {
		for (Experience e: experiences) add(e, timestamp);
	}
}
