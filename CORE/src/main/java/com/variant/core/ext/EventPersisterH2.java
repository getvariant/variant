package com.variant.core.ext;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import com.variant.core.config.VariantProperties;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.jdbc.EventPersisterJdbc;
import com.variant.open.securestring.SecureString;

public class EventPersisterH2 extends EventPersisterJdbc {
	
	String url = null;
	String user = null;
	SecureString password = null;  // TODO: change to securestring
	
	@Override
	public void initialized(Map<String,String> initParameters) throws Exception {
		url = initParameters.get("url");
		if (url == null) 
			throw new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName());
		user = initParameters.get("user");
		if (url == null) 
			throw new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "user", this.getClass().getName());
		String passString = initParameters.get("password");
		if (passString == null) 
			throw new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "password", this.getClass().getName(), VariantProperties.Keys.EVENT_PERSISTER_CLASS_INIT.propName());
		password = new SecureString(passString.toCharArray());
	}

	@Override
	public Connection getJdbcConnection() throws Exception {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection(url, user, new String(password.getValue()));
	}


}
