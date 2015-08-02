package com.variant.ext.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.variant.core.VariantInternalException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
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
	public Collection<Experience> getAll() {
		ArrayList<Experience> result = new ArrayList<Test.Experience>();
		for (Entry entry: entries) {
			for (Experience e: result) {
				if (entry.experience.getTest().equals(e.getTest())) {
					throw new VariantInternalException(
							"Cannoet add experience [" + entry.experience +
							"] because experience [" + e + "] already added");
				}
			}
			result.add(entry.experience);
		}
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * 
	 * @param test
	 * @return
	 */
	@Override
	public Experience get(Test test) {
		for (Entry e: entries) 
			if(e.experience.getTest().equals(test)) return e.experience;
		return null;
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	//@Override
	public Experience remove(Experience experience) {
		Iterator<Entry> iter = entries.iterator();
		while (iter.hasNext()) {
			Entry e = iter.next();
			if (e.experience.equals(experience)) {
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
	public void removeAll(Collection<Experience> entries) {
		for (Experience e: entries) remove(e);
	}

	/**
	 * 
	 * @param experience
	 * @return
	 */
	@Override
	public Experience add(Experience experience) {
		Experience result = remove(experience);
		entries.add(new Entry(experience));
		return result;
	}

	
}
