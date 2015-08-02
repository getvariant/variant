package com.variant.core.session;

import java.util.Collection;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public interface TargetingPersister {
	
	public void initialized(Config config, UserData...userArgs);
	
	/**
	 * Read all persisted experiences;
	 * Implementation must guarantee that all experiences are pairwise independent,
	 * i.e. there are no two experiences in the returned collection that belong
	 * to the same test.
	 * 
	 * @return Collection of <code>Test.Experience</code> corresponding to all text experiences
	 *         currently persisted by this object.
	 */
	public Collection<Experience> getAll();
	
	/**
	 * Read a test experience corresponding to a test
	 * @param test
	 * @return
	 */
	public Experience get(Test test);
		
	/**
	 * Delete the entry corresponding to a test. If the entry existed, the experience will be returned.
	 * @param experience
	 *
	public Experience remove(Test test);

	/**
	 * Delete entries.
	 * @param experience
	 */
	public void removeAll(Collection<Experience> entries);

	/**
	 * Save an experience. Will replace a currently persisted experience of one existed for this test.
	 * @param experience
	 */
	public Experience add(Experience experience);
	
	/**
	 * Configuration object
	 * @author Igor
	 *
	 */
	public static class Config {
		
		private int maxIdleDays = 30;
		private String className = "com.variant.ext.persist.TargetingPersisterInMemoryString";

		public Config() {}
		
		public void setMaxIdleDays(int days) {
			this.maxIdleDays = days;
		}
		
		public int getMaxIdleDays() {
			return maxIdleDays;
		}
		
		public void setClassName(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return className;
		}

	}
	
	/**
	 * Marker interface to denote user data to be passed to the initialized() method;
	 * @author Igor.
	 *
	 */
	public static interface UserData {};
}
