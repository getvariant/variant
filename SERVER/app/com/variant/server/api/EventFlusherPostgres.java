package com.variant.server.api;

import static com.variant.server.api.ConfigKeys.EVENT_FLUSHER_CLASS_INIT;
import static com.variant.server.boot.ServerErrorLocal.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.variant.core.EventFlusher;
import com.variant.server.jdbc.EventFlusherJdbc;
import com.variant.server.jdbc.JdbcService.Vendor;

/**
 * <p>An implementation of {@link EventFlusher}, which writes Variant events to an
 * instance of PostgreSQL database. Requires database schema, as created by the
 * {@code create-schema.sql} SQL script, included in the distribution. The database
 * server URL and login credentials must be supplied in the
 * {@code event.flasher.class.init} system property as a JSON string of the following
 * format:
 * <pre>
 * {@code
 *  {"url":pg-server-url,"user":database-user,"password":database-password}
 * }
 * </pre>
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class EventFlusherPostgres extends EventFlusherJdbc {
	
	private String url = null;
	private String user = null;
	private String password = null;
	
	@Override
	public void init(ConfigObject config) throws Exception {
				
		ConfigValue val = config.get("url");
		if (val == null)
			throw new ServerException.User(
					CONFIG_PROPERTY_NOT_SET, "url", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);
		url = (String) val.unwrapped(); 		// TODO: This will break if url exists but is no a string.

		val = config.get("user");
		if (val == null)
			throw new ServerException.User(
					CONFIG_PROPERTY_NOT_SET, "user", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);
		user = (String) val.unwrapped(); 		// TODO: This will break if url exists but is no a string.

		val = config.get("password");
		if (val == null)
			throw new ServerException.User(
					CONFIG_PROPERTY_NOT_SET, "password", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);
		password = (String) val.unwrapped(); 	// TODO: This will break if url exists but is no a string.

	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		return DriverManager.getConnection(url, props);		
	}

	@Override
	protected Vendor getJdbcVendor() {
		return Vendor.POSTGRES;
	}

}
