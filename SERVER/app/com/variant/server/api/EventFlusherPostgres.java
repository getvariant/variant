package com.variant.server.api;

import static com.variant.core.impl.CommonError.CONFIG_PROPERTY_NOT_SET;
import static com.variant.server.api.ConfigKeys.EVENT_FLUSHER_CLASS_INIT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.typesafe.config.Config;
import com.variant.server.jdbc.EventFlusherJdbc;
import com.variant.server.jdbc.JdbcService.Vendor;

/**
 * An implementation of {@link EventFlusher}, which writes Variant events to an 
 * instance of PostgreSQL database. The required database schema can be created by the
 * {@code create-schema.sql} SQL script, included with Variant server. 
 * <p>
 * Configuration.<br/>You may use the <code>variant.event.flusher.class.init</code> configuration property to pass configuration details to this object.
 * 
 * <ul>
 *  <li><code>url</code> - specifies the URL to the Postgres database.
 *  <li><code>user</code> - the Postgres database user.
 *  <li><code>password</code> - the Postgres database user's password.
 * </ul>
 * Example:<br/>
 * <code>variant.event.flusher.class.init = {"url":"jdbc:postgresql://localhost/variant","user":"variant","password":"variant"}</code>
 * @since 0.5
 */
public class EventFlusherPostgres extends EventFlusherJdbc {
	
	private Connection conn = null;

	public EventFlusherPostgres(Config config) throws Exception {
				
		String url = config.getString("url");
		if (url == null)
			throw new ServerException.Local(
					CONFIG_PROPERTY_NOT_SET, "url", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);

		String user = config.getString("user");
		if (user == null)
			throw new ServerException.Local(
					CONFIG_PROPERTY_NOT_SET, "user", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);

		String password = config.getString("password");
		if (password == null)
			throw new ServerException.Local(
					CONFIG_PROPERTY_NOT_SET, "password", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);

		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		conn = DriverManager.getConnection(url, props);		
		
	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		return conn;
	}

	@Override
	protected Vendor getJdbcVendor() {
		return Vendor.POSTGRES;
	}

}
