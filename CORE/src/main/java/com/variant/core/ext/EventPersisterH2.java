package com.variant.core.ext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.variant.core.jdbc.EventPersisterJdbc;

public class EventPersisterH2 extends EventPersisterJdbc {

	private Config config = null;
	
	@Override
	public void initialized(Config config) {
		this.config = config;
	}

	@Override
	public Connection getJdbcConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPassword());
	}


}
