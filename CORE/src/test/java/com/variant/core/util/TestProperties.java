package com.variant.core.util;

public class TestProperties {

	private static ApplicationProperties props = new ApplicationProperties("/local-test.properties", "/test.properties");

	/**
	 * Static singleton
	 */
	private TestProperties() {}

	public static String persisterClassName() {
		return props.getString("persister.class.name");
	}
	
	/**
	 * 
	 * @return
	 */
	public static String jdbcUrl() {
		return props.getString("jdbc.url");
	}

	/**
	 * 
	 * @return
	 */
	public static String jdbcUser() {
		return props.getString("jdbc.user");
	}

	/**
	 * 
	 * @return
	 */
	public static String jdbcPassword() {
		return props.getString("jdbc.password");
	}

}
