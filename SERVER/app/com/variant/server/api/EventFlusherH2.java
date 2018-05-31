package com.variant.server.api;


import static com.variant.core.impl.CommonError.CONFIG_PROPERTY_NOT_SET;
import static com.variant.server.api.ConfigKeys.EVENT_FLUSHER_CLASS_INIT;

import java.sql.Connection;
import java.sql.DriverManager;

import com.typesafe.config.Config;
import com.variant.server.jdbc.EventFlusherJdbc;
import com.variant.server.jdbc.JdbcService.Vendor;

/**
 * An implementation of {@link EventFlusher}, which writes Variant events to an 
 * instance of H2 database. The required database schema can be created by the
 * {@code create-schema.sql} SQL script, included with Variant server. 
 * <p>
 * Configuration.<br/>You may use the <code>variant.event.flusher.class.init</code> configuration property to pass configuration details to this object.
 * 
 * <ul>
 *  <li><code>url</code> - specifies the URL to the H2 database.
 *  <li><code>user</code> - the H2 database user.
 *  <li><code>password</code> - the H2 database user's password.
 * </ul>
 * Example:<br/>
 * <code>variant.event.flusher.class.init = {"url":"jdbc:h2:mem:variant;MVCC=true;DB_CLOSE_DELAY=-1;","user":"variant","password":"variant"}</code>
 * 
 * @since 0.5
 */
public class EventFlusherH2 extends EventFlusherJdbc {
	
	String url = null;
	String user = null;
	String password = null;
	
	public EventFlusherH2(Config config) throws Exception {
		
		url = config.getString("url");
		if (url == null)
			throw new ServerException.Local(
					CONFIG_PROPERTY_NOT_SET, "url", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);

		user = config.getString("user");
		if (user == null)
			throw new ServerException.Local(
					CONFIG_PROPERTY_NOT_SET, "user", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);

		password = config.getString("password");
		if (password == null)
			throw new ServerException.Local(
					CONFIG_PROPERTY_NOT_SET, "password", getClass().getName(), EVENT_FLUSHER_CLASS_INIT);
		
	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection(url, user, password);
	}

	@Override
	protected Vendor getJdbcVendor() {
		return Vendor.H2;
	}
	
}
