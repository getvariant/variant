package com.variant.core.util;

import com.variant.core.jdbc.JdbcService;

public class VariantProperties {

	private static ApplicationProperties props = new ApplicationProperties("/variant.props");

	/**
	 * Static singleton
	 */
	protected VariantProperties() {}

	public static String persisterClassName() {
		return props.getString("persister.class.name");
	}
	
	/**
	 * 
	 * @return
	 */
	public static JdbcService.Vendor jdbcVendor() {
		return JdbcService.Vendor.valueOf(props.getString("jdbc.vendor").toUpperCase());
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
