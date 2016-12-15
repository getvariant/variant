package com.variant.server.event;


import static com.variant.server.ServerPropertiesKey.EVENT_FLUSHER_CLASS_INIT;

import java.sql.Connection;
import java.sql.DriverManager;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.variant.server.ServerProperties;

import static com.variant.server.ServerError.*;

import com.variant.server.ServerErrorException;
import com.variant.server.ServerPropertiesKey;
import com.variant.server.jdbc.EventFlusherJdbc;
import com.variant.server.jdbc.JdbcService.Vendor;

/**
 * <p>An implementation of {@link EventFlusher}, which writes Variant events to an 
 * instance of H2 database. Requires database schema, as created by the
 * {@code create-schema.sql} SQL script, included in the distribution. The database
 * server URL and login credentials must be supplied in the
 * {@code event.flasher.class.init} system property as a JSON string of the following
 * format:
 * <pre>
 * {@code
 *  {"url":h2-server-url,"user":database-user,"password":database-password}
 * }
 * </pre>
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class EventFlusherH2 extends EventFlusherJdbc {
	
	String url = null;
	String user = null;
	String password = null;
	
	@Override
	public void init(ServerProperties props) throws Exception {
		
		ConfigObject conf = props.getObject(ServerPropertiesKey.EVENT_FLUSHER_CLASS_INIT);
		
		ConfigValue val = conf.get("url");
		if (val == null)
			throw new ServerErrorException(
					CONFIG_PROPERTY_NOT_SET, "url", getClass().getName(), EVENT_FLUSHER_CLASS_INIT.getExternalName());
		url = (String) val.unwrapped(); 		// TODO: This will break if url exists but is no a string.

		val = conf.get("user");
		if (val == null)
			throw new ServerErrorException(
					CONFIG_PROPERTY_NOT_SET, "user", getClass().getName(), EVENT_FLUSHER_CLASS_INIT.getExternalName());
		user = (String) val.unwrapped(); 		// TODO: This will break if url exists but is no a string.

		val = conf.get("password");
		if (val == null)
			throw new ServerErrorException(
					CONFIG_PROPERTY_NOT_SET, "password", getClass().getName(), EVENT_FLUSHER_CLASS_INIT.getExternalName());
		password = (String) val.unwrapped(); 	// TODO: This will break if url exists but is no a string.
		
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
