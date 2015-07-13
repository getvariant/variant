package com.variant.core.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.variant.core.Variant;
import com.variant.core.conf.VariantProperties;
import com.variant.core.event.EventPersister;
import com.variant.core.util.VariantStringUtils;


public class JdbcUtil {

	/**
	 * Read a SQL script from a resource file, discard comments and parse it out into
	 * individual statements that can be executed via JDBC.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private static List<String> statementsFromResourceFile(String name) throws IOException {
		
		InputStream schemaIS = JdbcUtil.class.getResourceAsStream(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(schemaIS));
		
		// read line by line and extract individual statements, as they are separated by ';'
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder currentStatement = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			// skip comments.
			String[] tokens = line.split("\\-\\-");
			tokens = tokens[0].split(";", -1);  // include trailing empty string if ';' is last char.
			currentStatement.append(' ').append(tokens[0]);
			for (int i = 1; i < tokens.length; i++) {
				String stmt = currentStatement.toString().trim();
				if (stmt.length() > 0) result.add(stmt);
				currentStatement.setLength(0);
				currentStatement.append(' ').append(tokens[i]);
			}
			line = reader.readLine();
		} 
		if (currentStatement.length() > 0) {
			String stmt = currentStatement.toString().trim();
			if (stmt.length() > 0) result.add(stmt);
		}

		return result;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * This is how we get to the underlying jdbc connection via the EvetWriter facade.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
	
		EventPersister ep = Variant.getEventWriter().getEventPersister();
		return ((EventPersisterJdbc) ep).getJdbcConnection();

	}
	
	/**
	 * Drop relational schema. Ignore the table does not exist errors.
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public static void dropSchema() throws Exception {
		
		final String[] SQL_STATES_OBJECT_DOES_NOT_EXIST = 
				VariantProperties.jdbcVendor() == JdbcService.Vendor.POSTGRES ? new String[] {"42P01"} :
				VariantProperties.jdbcVendor() == JdbcService.Vendor.H2       ? new String[] {"42S02", "90036"} : null;
		
		List<String> statements = statementsFromResourceFile("/db/drop-schema.sql");
		Statement jdbcStmt = getConnection().createStatement();

		for (String stmt: statements) {
			
			String[] tokens = stmt.split(" ");
			
			try {
				jdbcStmt.execute(stmt);
				Variant.getLogger().debug(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... OK.");
			}
			catch (SQLException e) {				
				System.out.println(e.getSQLState());
				if (VariantStringUtils.equalsIgnoreCase(e.getSQLState(), SQL_STATES_OBJECT_DOES_NOT_EXIST)) {
					Variant.getLogger().debug(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... Relation Does Not Exist.");
				}
				else throw e;
			}
		}
	}

	/**
	 * Create relational schema
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public static void createSchema() throws Exception {
		
		List<String> statements = statementsFromResourceFile("/db/create-schema.sql");
		Statement jdbcStmt = getConnection().createStatement();
		for (String stmt: statements) {
			jdbcStmt.execute(stmt);
			String[] tokens = stmt.split(" ");
			Variant.getLogger().debug(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... OK.");
		}
	}
	
	/**
	 * Create relational schema
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public static void recreateSchema() throws Exception {
		
		dropSchema();
		createSchema();
		
	}

}
