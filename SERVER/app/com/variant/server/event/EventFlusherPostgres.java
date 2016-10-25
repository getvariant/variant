package com.variant.server.event;

import static com.variant.core.xdm.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.exception.VariantRuntimeUserErrorException;
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
	public void init(VariantCoreInitParams initParams) throws Exception {
		url = (String) initParams.getOrThrow(
				"url", new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName()));
		
		user = (String) initParams.getOrThrow(
				"user", new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "user", this.getClass().getName()));
		
		password = (String) initParams.getOrThrow(
				"password", new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "password", this.getClass().getName()));

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
