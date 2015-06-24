package com.variant.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import com.variant.core.VariantInternalException;
import com.variant.core.event.BaseEvent;
import com.variant.core.event.EventExperience;
import com.variant.core.event.EventPersister;
import com.variant.core.util.JdbcUtil;

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
			    "VALUES (events_id_seq.NEXTVAL, ?, ?, ?, ?, ?)";

		/*
		 * event_id              CHAR(32) REFERENCES events(id) ON DELETE CASCADE,
  test_name             VARCHAR(128) NOT NULL,     -- Test name
  experience_name       VARCHAR(128) NOT NULL,     -- Experience name
  is_experience_control BOOLEAN NOT NULL,          -- Is experience control for the test?
  is_view_invariant     BOOLEAN,                   -- If event is a view serve event, is this view invariant for this test?
  view_resolved_path    VARCHAR(256),              -- If event is a view serve event, the view's actual path; null otherwise.
		 */
		final String INSERT_EVENTS_EXPERIENCES_SQL = 
				"INSERT INTO events_experiences " +
			    "(event_id, test_name, experience_name, is_control, is_view_invariant, view_resolved_path) " +
			    "VALUES (?, ?, ?, ?, ?, ?)";

		JdbcService.executeUpdate(
			JdbcUtil.getConnection(), 
			new JdbcService.UpdateOperation() {

				@Override
				public void execute(Connection conn) throws SQLException {

					//
					// 1. Insert into EVENTS and get the sequence generated IDs back.
					//
					PreparedStatement stmt = conn.prepareStatement(INSERT_EVENTS_SQL);

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
					stmt = conn.prepareStatement(INSERT_EVENTS_EXPERIENCES_SQL);
					
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
				}
			}
		);
		
	}
	

}
