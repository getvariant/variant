package com.variant.server.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import com.variant.core.schema.Variation.Experience;
import com.variant.server.api.TraceEventFlusher;
import com.variant.server.api.FlushableTraceEvent;
import com.variant.server.api.ServerException;
import com.variant.server.jdbc.JdbcService.Vendor;


/**
 * JDBC event flushers extend this class instead of implementing the EventFlusher interface. 
 * All the JDBC work is done here, leaving the concrete subclasses with just the task of
 * creating a database connection for the particular JDBC implementation.
 * 
 * @author Igor.
 *
 */
abstract public class TraceEventFlusherJdbc implements TraceEventFlusher {
				
	/**
	 * Concrete subclass tells this class how to obtain a connection to its flavor of JDBC.
	 * JUnits will also use this to create the schema.
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract Connection getJdbcConnection() throws Exception;

	/**
	 * Implementations will know the vendor.
	 * @return
	 */
	protected abstract Vendor getJdbcVendor();
	
	/**
	 * Persist a collection of events.
	 */
	@Override
	final public void flush(final Collection<FlushableTraceEvent> events) throws Exception {
				
		final String INSERT_EVENTS_SQL = 
				"INSERT INTO events " +
			    "(id, session_id, created_on, event_name) " +
				(getJdbcVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('events_id_seq'), ?, ?, ?)" :
				getJdbcVendor() == Vendor.H2 ?
						"VALUES (events_id_seq.NEXTVAL, ?, ?, ?)" : "DUNNO");

		final String INSERT_EVENT_EXPERIENCES_SQL = 
				"INSERT INTO event_experiences " +
			    "(id, event_id, test_name, experience_name, is_control) " +
				(getJdbcVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('event_experiences_id_seq'), ?, ?, ?, ?)" :
				getJdbcVendor() == Vendor.H2 ?
						"VALUES (event_experiences_id_seq.NEXTVAL, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_PARAMETERS_SQL = 
				"INSERT INTO event_attributes " +
			    "(event_id, key, value) " +
			    "VALUES (?, ?, ?)";

		JdbcAdapter.executeUpdate(
			getJdbcConnection(), 
			new JdbcAdapter.UpdateOperation() {

				@Override
				public void execute(Connection conn) throws SQLException {

					//
					// 1. Insert into EVENTS and get the sequence generated IDs back.
					//
					
					PreparedStatement stmt = conn.prepareStatement(INSERT_EVENTS_SQL, Statement.RETURN_GENERATED_KEYS);

					for (FlushableTraceEvent event: events) {
						stmt.setString(1, event.getSessionId());
						stmt.setTimestamp(2, new Timestamp(event.getCreateDate().getTime()));
						stmt.setString(3, event.getName());

						stmt.addBatch();
					}
					
					// Send rows to the database.
					stmt.executeBatch();

					// Read sequence generated event IDs and add them to the event objects.
					// We'll need these ids when inserting into events_experiences.
					long[] eventIds = new long[events.size()];
					int index = 0;
					ResultSet gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (index == events.size()) 
							throw new ServerException.Internal("Received more genereated keys than inserted event records.");
						
						eventIds[index++] = gennedKeys.getLong(1);
					}
					if (index < events.size()) 
						throw new ServerException.Internal("Received fewer genereated keys than inserted event records.");

					stmt.close();

					//
					// 2. Insert into EVENT_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_PARAMETERS_SQL);
					index = 0;
					for (FlushableTraceEvent event: events) {
						long eventId = eventIds[index++];
						for (Map.Entry<String, String> param: event.getAttributes().entrySet()) {

							stmt.setLong(1, eventId);
							stmt.setString(2, param.getKey());
							stmt.setString(3, param.getValue().toString());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();
					stmt.close();
					
					//
					// 3. Insert into EVENT_EXPERIENCES.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_EXPERIENCES_SQL);
					index = 0;
					for (FlushableTraceEvent event: events) {
						long eventId = eventIds[index++];
						for (Experience exp: event.getLiveExperiences()) {
							stmt.setLong(1, eventId);
							stmt.setString(2, exp.getVariation().getName());
							stmt.setString(3, exp.getName());
							stmt.setBoolean(4, exp.isControl());						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();					
					stmt.close();
				}
			}
		);
		
	}
}
