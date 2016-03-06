package com.variant.core.ext;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import java.sql.Connection;
import java.sql.DriverManager;

import com.variant.core.InitializationParams;
import com.variant.core.VariantProperties;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.jdbc.EventPersisterJdbc;
import com.variant.core.jdbc.JdbcService.Vendor;
import com.variant.open.securestring.SecureString;

public class EventPersisterH2 extends EventPersisterJdbc {
	
	String url = null;
	String user = null;
	SecureString password = null;  // TODO: change to securestring
	
	@Override
	public void initialized(InitializationParams initParams) throws Exception {
		url = initParams.getOrThrow(
				"url", 
				new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName(), VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()));
		user = initParams.getOrThrow(
				"user", 
				new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "user", this.getClass().getName(), VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()));
		String passString = initParams.getOrThrow(
				"password", 
				new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "password", this.getClass().getName(), VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()));
		password = new SecureString(passString.toCharArray());
	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection(url, user, new String(password.getValue()));
	}

	@Override
	public Vendor getVendor() {
		return Vendor.H2;
	}
	
}
