package com.variant.core.conf;

import java.util.HashMap;

import com.variant.core.jdbc.JdbcService;

public class VariantProperties {

	private static ApplicationProperties props = new ApplicationProperties("/variant.props");

	// Construct the map of defaults
	static {
		@SuppressWarnings("serial")
		HashMap<String, String> defaults  = new HashMap<String, String>() {{
			put("default.idle.days.to.live", "30");
		}};
		
		props.setDefaults(defaults);
	}
	
	/**
	 * Static singleton
	 */
	protected VariantProperties() {}

	/**
	 * 
	 * @return
	 */
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

	public static int defaultIdleDaysToLive() {
		return props.getInteger("default.idle.days.to.live");
	}
}
