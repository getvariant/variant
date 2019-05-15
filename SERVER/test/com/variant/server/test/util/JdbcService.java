package com.variant.server.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.util.IoUtils;
import com.variant.core.util.StringUtils;
import com.variant.server.impl.TraceEventWriter;


public class JdbcService {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcService.class);

	// Comment .....
	private static List<String> parseSQLScript(InputStream is) throws IOException {
		
		BufferedReader lines = new BufferedReader(new InputStreamReader(is));
		
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder currentStatement = new StringBuilder();
		String line = lines.readLine();
		while (line != null) {
			//System.out.println("*** " + line);
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
			line = lines.readLine();
		} 
		if (currentStatement.length() > 0) {
			String stmt = currentStatement.toString().trim();
			if (stmt.length() > 0) result.add(stmt);
		}

		return result;
	}

	/**
	 * Exception parser.
	 * @param e
	 * @param statement
	 * @throws SQLException
	 */
	private void parseSQLException(SQLException e, String statement) throws SQLException {

		Vendor vendor = getVendor();

		final String[] SQL_STATES_OBJECT_DOES_NOT_EXIST = 
				vendor == Vendor.POSTGRES ? new String[] {"42P01"} :
				(vendor == Vendor.H2 ? new String[] {"90036"} : null);

		final String[] SQL_STATES_OBJECT_ALREADY_EXIST = 
				vendor == Vendor.POSTGRES ? new String[] {"42P07"} : new String[] {"42S01"};

		String[] tokens = statement.split(" ");

		if (StringUtils.equalsIgnoreCase(e.getSQLState(), SQL_STATES_OBJECT_DOES_NOT_EXIST)) {
			LOG.error(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... Object Does Not Exist.");
		}
		else if (StringUtils.equalsIgnoreCase(e.getSQLState(), SQL_STATES_OBJECT_ALREADY_EXIST)) {
			LOG.error(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... Object Already Exists.");
		}
		else {
			LOG.error(String.format("SQLException: %s, (%s error code %s)",  e.getMessage(), vendor.name(), e.getSQLState()));
			throw e;
		}

	}

	private TraceEventWriter eventWriter;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @author Igor
	 *
	 */
	public static enum Vendor { POSTGRES, H2, MYSQL}

	public JdbcService(TraceEventWriter eventWriter) {
		this.eventWriter = eventWriter;
	}

	/**
	 * 
	 * @return
	 */
	public Vendor getVendor() {
		// Figure out the JDBC vendor, if we can.
		if (eventWriter.flusher().getClass().getSimpleName().equals("TraceEventFlusherPostgres")) {
			return Vendor.POSTGRES;
		}
		else if (eventWriter.flusher().getClass().getSimpleName().equals("TraceEventFlusherH2")) {
			return Vendor.H2;
		}
		else return null;
	}
	
	/**
	 * Connect to database.
	 */
	public Connection getConnection() throws Exception {
		
		String url = null;
		switch (getVendor()) {
		case POSTGRES:
			url = "jdbc:postgresql://localhost/variant?user=variant&password=variant";
			break;
		case H2:
			url = "jdbc:h2:mem:variant;MVCC=true;DB_CLOSE_DELAY=-1;user=variant;password=variant";
			break;
		}
		
		return DriverManager.getConnection(url);
	}
		
	/**
	 * Drop relational schema. Ignore the table does not exist errors.
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public void dropSchema() throws Exception {
				
		List<String> statements = parseSQLScript(IoUtils.openResourceAsStream("/db/h2/drop-schema.sql")._1());
		Statement jdbcStmt = getConnection().createStatement();

		for (String stmt: statements) {
			
			String[] tokens = stmt.split(" ");
			
			try {
				jdbcStmt.execute(stmt);
				LOG.debug(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... OK.");
			}
			catch (SQLException e) {
				parseSQLException(e, stmt);
			}
		}
	}

	/**
	 * Create relational schema
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public void createSchema() throws Exception {
		
		List<String> statements = parseSQLScript(IoUtils.openResourceAsStream("/db/h2/create-schema.sql")._1());
		Statement jdbcStmt = getConnection().createStatement();
		for (String stmt: statements) {
			try {
				jdbcStmt.execute(stmt);
				String[] tokens = stmt.split(" ");
				LOG.debug(tokens[0] + " " + tokens[1] + " " + tokens[2] + "... OK.");
			}
			catch (SQLException e) {
				parseSQLException(e, stmt);
			}
		}
	}
	
	/**
	 * Create relational schema
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public void recreateSchema() throws Exception {
		dropSchema();
		createSchema();
	}

	/**
	 * Set nullable Integter
	 * @param statement
	 * @param pos
	 * @param value
	 * @throws SQLException 
	 */
	public static void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
		if (value == null) statement.setNull(index, Types.INTEGER);
		else statement.setInt(index, value);
	}

}
