package com.variant.core.ext;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.jdbc.EventPersisterJdbc;
import com.variant.core.jdbc.JdbcService.Vendor;
import com.variant.open.securestring.SecureString;

public class EventPersisterPostgres extends EventPersisterJdbc {
	
	private String url = null;
	private String user = null;
	private SecureString password = null;
	
	@Override
	public void initialized(VariantCoreInitParams initParams) throws Exception {
		url = (String) initParams.getOrThrow(
				"url", new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName()));
		
		user = (String) initParams.getOrThrow(
				"user", new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "user", this.getClass().getName()));
		
		String passString = (String) initParams.getOrThrow(
				"password", new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "password", this.getClass().getName()));

		password = new SecureString(passString.toCharArray());
	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", new String(password.getValue()));
		return DriverManager.getConnection(url, props);		
	}

	@Override
	public Vendor getVendor() {
		return Vendor.POSTGRES;
	}

}
