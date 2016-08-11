package com.variant.core.event;

import static com.variant.core.VariantCorePropertyKeys.EVENT_FLUSHER_CLASS_INIT;
import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import java.sql.Connection;
import java.sql.DriverManager;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.jdbc.EventFlusherJdbc;
import com.variant.core.jdbc.JdbcService.Vendor;
import com.variant.open.securestring.SecureString;

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
	SecureString password = null;  // TODO: change to securestring
	
	@Override
	public void init(VariantCoreInitParams initParams) throws Exception {
		url = (String) initParams.getOrThrow(
				"url", 
				new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName(), EVENT_FLUSHER_CLASS_INIT.propertyName()));
		
		user = (String) initParams.getOrThrow(
				"user", 
				new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "user", this.getClass().getName(), EVENT_FLUSHER_CLASS_INIT.propertyName()));
		
		String passString = (String) initParams.getOrThrow(
				"password", 
				new VariantRuntimeUserErrorException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "password", this.getClass().getName(), EVENT_FLUSHER_CLASS_INIT.propertyName()));
		password = new SecureString(passString.toCharArray());
	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection(url, user, new String(password.getValue()));
	}

	@Override
	protected Vendor getJdbcVendor() {
		return Vendor.H2;
	}
	
}
