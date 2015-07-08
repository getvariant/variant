package com.variant.core.session;

import java.util.Collection;

import com.variant.core.schema.Test;

public interface TargetingPersister {
	
	public void initialized(Config config, UserData...userArgs);
	
	/**
	 * Read all persisted experiences;
	 * @return Collection of <code>Test.Experience</code> corresponding to all text experiences
	 *         currently persisted by this object.
	 */
	Collection<Test.Experience> readAll();
	
	/**
	 * Read a test experience corresponding to a test
	 * @param test
	 * @return
	 */
	Test.Experience read(Test test);
		
	/**
	 * Delete the entry corresponding to a test. If the entry existed, the experience will be returned.
	 * @param experience
	 */
	Test.Experience remove(Test test);

	/**
	 * Save an experience. Will replace a currently persisted experience of one existed for this test.
	 * @param experience
	 */
	Test.Experience write(Test.Experience experience);
	
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
