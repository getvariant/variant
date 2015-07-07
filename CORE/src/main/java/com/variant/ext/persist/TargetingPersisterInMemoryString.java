package com.variant.ext.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.variant.core.config.Test;
import com.variant.core.config.Test.Experience;
import com.variant.core.session.TargetingPersister;

/**
 * Basic implementation of a targeting persister as a String in memory.
 * Suitable for testing purposes only.  A real implementation, such as
 * Servlet, will use browser cookies.
 * 
 * @author Igor
 *
 */
public class TargetingPersisterInMemoryString implements TargetingPersister {

	private static class Entry {
		private Experience experience;
		private long timestamp;
		private Entry(Experience experience) {
			this.experience = experience;
			timestamp = System.currentTimeMillis();
		}
	}

	private Config config;
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	/**
	 * 
	 * @param config
	 */
	@Override
	public void initialized(Config config, UserData...userArgs) {
		this.config = config;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Collection<Test.Experience> readAll() {
		ArrayList<Test.Experience> result = new ArrayList<Test.Experience>(entries.size());
		for (Entry e: entries) 
			result.add(e.experience);
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	@Override
	public Experience read(Test test) {
		for (Entry e: entries) 
			if(e.experience.getTest().equals(test)) return e.experience;
		return null;
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	@Override
	public Experience remove(Test test) {
		Iterator<Entry> iter = entries.iterator();
		while (iter.hasNext()) {
			Entry e = iter.next();
			if (e.experience.getTest().equals(test)) {
				iter.remove();
				return e.experience;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	@Override
	public Experience write(Experience experience) {
		Experience result = remove(experience.getTest());
		entries.add(new Entry(experience));
		return result;
	}

	
}
