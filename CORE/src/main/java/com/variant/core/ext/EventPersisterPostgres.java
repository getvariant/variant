package com.variant.core.ext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.variant.core.VariantProperties;
import com.variant.core.jdbc.EventPersisterJdbc;

public class EventPersisterPostgres extends EventPersisterJdbc {
	
	@Override
	public void initialized() {}

	@Override
	public Connection getJdbcConnection() throws ClassNotFoundException, SQLException {
	
		String url = VariantProperties.eventPersisterJdbcUrl();
		Properties props = new Properties();
		props.setProperty("user", VariantProperties.eventPersisterJdbcUser());
		props.setProperty("password", VariantProperties.eventPersisterJdbcPassword());
		return DriverManager.getConnection(url, props);		
	}

}
