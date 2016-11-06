package com.variant.server.event;


import static com.variant.core.xdm.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;
import static com.variant.server.ServerPropertiesKey.EventFlusherClassInit;

import java.sql.Connection;
import java.sql.DriverManager;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.exception.VariantRuntimeUserErrorException;
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
	public void init(VariantCoreInitParams initParams) throws Exception {
		url = (String) initParams.getOr(
				"url", 
				new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", getClass().getName(), EventFlusherClassInit.name));
		
		user = (String) initParams.getOr(
				"user", 
				new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "user", getClass().getName(), EventFlusherClassInit.name));
		
		password = (String) initParams.getOr(
				"password", 
				new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "password", getClass().getName(), EventFlusherClassInit.name));
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
