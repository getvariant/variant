package com.variant.core.event;

import java.util.Collection;

import com.variant.core.conf.VariantProperties;

/**
 * 
 * @author Igor
 *
 */
public interface EventPersister {

	/**
	 * Will be called by the Variant container upon initialization.
	 * Client code may use this for further initialization.
	 * @param args - strings passed to the configuration.
	 */
	public void initialized(Config config);
	
	/**
	 * Persist a bunch of events somewhere.
	 */
	public void persist(Collection<VariantEventSupport> events) throws Exception;
	
	/**
	 * Configurator class. Its instance is passed to the initialized() method above
	 * @author Igor
	 *
	 */
	public static class Config {
		
		private Object[] userArgs = null;
		
		// Defaults from system props.
		// May still be manipulated programmatically via setters.
		private String eventPersisterClassName = VariantProperties.eventPersisterClassName();
		private String jdbcUrl = VariantProperties.eventPersisterJdbcUrl();
		private String jdbcUser = VariantProperties.eventPersisterJdbcUser();
		private String jdbcPassword = VariantProperties.eventPersisterJdbcPassword();
		
		public Config () {}
		
		public Object[] getUserArgs() {
			return userArgs;
		}

		public void setEventPersisterClassName(String eventPersisterClassName) {
			this.eventPersisterClassName = eventPersisterClassName;
		}

		public String getEventPersisterClassName() {
			return eventPersisterClassName;
		}

		public void setUserArgs(Object[] userArgs) {
			this.userArgs = userArgs;
		}

		public String getJdbcUrl() {
			return jdbcUrl;
		}

		public void setJdbcUrl(String jdbcUrl) {
			this.jdbcUrl = jdbcUrl;
		}

		public String getJdbcUser() {
			return jdbcUser;
		}

		public void setJdbcUser(String jdbcUser) {
			this.jdbcUser = jdbcUser;
		}

		public String getJdbcPassword() {
			return jdbcPassword;
		}

		public void setJdbcPassword(String jdbcPassword) {
			this.jdbcPassword = jdbcPassword;
		}

	}
}
