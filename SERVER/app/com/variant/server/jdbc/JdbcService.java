package com.variant.server.jdbc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.util.StringUtils;
import com.variant.server.event.TraceEventWriter;


public class JdbcService {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcService.class);
	
	/**
	 * Read a SQL script from a resource file, discard comments and parse it out into
	 * individual statements that can be executed via JDBC.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private static List<String> statementsFromFile(String path) throws IOException {
		
		List<String> input = Files.readAllLines(Paths.get(path));
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder currentStatement = new StringBuilder();
		for (String line: input) {
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
	public static enum Vendor {
		POSTGRES,
		H2
	}

	public JdbcService(TraceEventWriter eventWriter) {
		if (eventWriter == null) throw new IllegalArgumentException("EventWriter cannot be null");
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
	 * Obtain the underlying JDBC connection via EventWriter.
	 */
	public Connection getConnection() throws Exception {
		return ((TraceEventFlusherJdbc)eventWriter.flusher()).getJdbcConnection();
	}
		
	/**
	 * Drop relational schema. Ignore the table does not exist errors.
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public void dropSchema() throws Exception {
				
		List<String> statements = statementsFromFile("distr/db/h2/drop-schema.sql");
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
		
		List<String> statements = statementsFromFile("distr/db/h2/create-schema.sql");
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
