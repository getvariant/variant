package com.variant.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import com.variant.core.VariantInternalException;
import com.variant.core.event.BaseEvent;
import com.variant.core.event.EventExperience;
import com.variant.core.event.EventPersister;
import com.variant.core.util.JdbcUtil;
import com.variant.core.util.VariantProperties;

/**
 * JDBC persisters extend this class instead of implementing the EventPersister interface. 
 * All the JDBC work is done here, leaving the concrete subclasses with just the task of
 * creating a database connection for the particular JDBC implementation.
 * 
 * @author Igor.
 *
 */
abstract public class EventPersisterJdbc implements EventPersister {
	
	/**
	 * Concrete subclass tells this class how to obtain a connection to its flavor of JDBC.
	 * JUnits will also use this to create the schema.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public abstract Connection getJdbcConnection() throws ClassNotFoundException, SQLException;

	/**
	 * Persist a collection of events.
	 */
	@Override
	final public void persist(final Collection<BaseEvent> events) throws Exception {

		final String INSERT_EVENTS_SQL = 
				"INSERT INTO events " +
			    "(id, session_id, created_on, event_name, event_value, status) " +
				(VariantProperties.jdbcVendor() == JdbcService.Vendor.POSTGRES ?
						"VALUES (NEXTVAL('events_id_seq'), ?, ?, ?, ?, ?)" :
				VariantProperties.jdbcVendor() == JdbcService.Vendor.H2 ?
						"VALUES (events_id_seq.NEXTVAL, ?, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_EXPERIENCES_SQL = 
				"INSERT INTO event_experiences " +
			    "(event_id, test_name, experience_name, is_control, is_view_invariant, view_resolved_path) " +
			    "VALUES (?, ?, ?, ?, ?, ?)";

		final String INSERT_EVENT_PARAMETERS_SQL = 
				"INSERT INTO event_params " +
			    "(event_id, param_key, param_value) " +
			    "VALUES (?, ?, ?)";

		JdbcService.executeUpdate(
			JdbcUtil.getConnection(), 
			new JdbcService.UpdateOperation() {

				@Override
				public void execute(Connection conn) throws SQLException {

					//
					// 1. Insert into EVENTS and get the sequence generated IDs back.
					//
					
					// Postgres requires Statement.RETURN_GENERATED_KEYS.
					PreparedStatement stmt = conn.prepareStatement(INSERT_EVENTS_SQL, Statement.RETURN_GENERATED_KEYS);

					for (BaseEvent event: events) {

						stmt.setString(1, event.getSessionId());
						stmt.setTimestamp(2, new Timestamp(event.getCreateDate().getTime()));
						stmt.setString(3, event.getEventName());
						stmt.setString(4, event.getEventValue());
						stmt.setInt(5, event.getStatus().ordinal());

						stmt.addBatch();
					}
					
					// Send rows to the database.
					stmt.executeBatch();
					
					// Read sequence generated event IDs and add them to the event objects.
					// We'll need these ids when inserting into events_experiences.
					Iterator<BaseEvent> eventsIter = events.iterator();
					ResultSet gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {
						if (!eventsIter.hasNext()) 
							throw new VariantInternalException("Received more genereated keys than inserted events.");
						
						BaseEvent event = eventsIter.next();
						event.setId(gennedKeys.getLong(1));
					}
					if (eventsIter.hasNext()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted events.");
					
					stmt.close();
					
					//
					// 2. Insert into EVENTS_EXPERIENCES.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_EXPERIENCES_SQL);
					
					for (BaseEvent event: events) {
						for (EventExperience ee: event.getEventExperiences()) {
	
							stmt.setLong(1, ee.getEventId());
							stmt.setString(2, ee.getTestName());
							stmt.setString(3, ee.getExperienceName());
							stmt.setBoolean(4, ee.isExpereinceControl());
							stmt.setBoolean(5, ee.isViewInvariant());
							stmt.setString(6, ee.getViewResolvedPath());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();
					
					//
					// 3. Insert into EVENTS_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_PARAMETERS_SQL);
					
					for (BaseEvent event: events) {
						for (String key: event.getParameterKeys()) {
	
							stmt.setLong(1, event.getId());
							stmt.setString(2, key);
							stmt.setString(3, event.getParameter(key));
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();

				}
			}
		);
		
	}
	

}
