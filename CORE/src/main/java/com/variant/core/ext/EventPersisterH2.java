package com.variant.core.ext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.variant.core.VariantProperties;
import com.variant.core.jdbc.EventPersisterJdbc;

public class EventPersisterH2 extends EventPersisterJdbc {
	
	@Override
	public void initialized() {}

	@Override
	public Connection getJdbcConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection(
				VariantProperties.eventPersisterJdbcUrl(), 
				VariantProperties.eventPersisterJdbcUser(), 
				VariantProperties.eventPersisterJdbcPassword());
	}


}
