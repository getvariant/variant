package com.variant.core.session;

import java.util.Collection;

import com.variant.core.VariantSession;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public interface TargetingPersister {
	
	/**
	 * Mechanism for passing user data to an instance of this class,
	 * after it has been initialized by the container.
	 * 
	 * @param config
	 * @param userArgs
	 */
	public void initialized(Config config, VariantSession ssn, UserData...userArgs);
	
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
	 */
	public Experience remove(Test test);

	/**
	 * Save an experience. Will replace a currently persisted experience of one existed for this test.
	 * @param experience
	 */
	public Experience add(Experience experience, long timestamp);
	
	/**
	 * Update the timestamp of this tests's entry, if any.
	 * @param test
	 */
	public void touch(Test test);
	
	/**
	 * Configuration object
	 *
	 */
	public static class Config {
		
		private int defaultMinIdleDays = 30;
		private String className = "com.variant.core.session.TargetingPersisterFromString";
		
		public Config() {}
		
		public void setDefaultMinIdleDays(int days) {
			defaultMinIdleDays = days;
		}
		
		public int getDefaultMinIdleDays() {
			return defaultMinIdleDays;
		}
		
		public void setMinIdleDays(String className) {
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
