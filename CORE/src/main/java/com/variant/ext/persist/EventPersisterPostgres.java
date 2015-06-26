package com.variant.ext.persist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.variant.core.jdbc.EventPersisterJdbc;

public class EventPersisterPostgres extends EventPersisterJdbc {

	private Config config = null;
	
	@Override
	public void initialized(Config config) {
		this.config = config;
	}

	@Override
	public Connection getJdbcConnection() throws ClassNotFoundException, SQLException {
	
		String url = config.getJdbcUrl();
		Properties props = new Properties();
		props.setProperty("user",config.getJdbcUser());
		props.setProperty("password",config.getJdbcPassword());
		return DriverManager.getConnection(url, props);		
	}

}
