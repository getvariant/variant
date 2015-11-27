package com.variant.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEventExperience;
import com.variant.core.event.VariantEventExperienceSupport;
import com.variant.core.event.VariantEventSupport;
import com.variant.core.exception.VariantInternalException;

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
	final public void persist(final Collection<VariantEventSupport> events) throws Exception {

		final String INSERT_EVENTS_SQL = 
				"INSERT INTO events " +
			    "(id, session_id, created_on, event_name, event_value) " +
				(getVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('events_id_seq'), ?, ?, ?, ?)" :
				getVendor() == Vendor.H2 ?
						"VALUES (events_id_seq.NEXTVAL, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_EXPERIENCES_SQL = 
				"INSERT INTO event_experiences " +
			    "(id, event_id, test_name, experience_name, is_control) " +
				(getVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('event_experiences_id_seq'), ?, ?, ?, ?)" :
				getVendor() == Vendor.H2 ?
						"VALUES (event_experiences_id_seq.NEXTVAL, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_PARAMETERS_SQL = 
				"INSERT INTO event_params " +
			    "(event_id, key, value) " +
			    "VALUES (?, ?, ?)";

		final String INSERT_EVENT_EXPERIENCE_PARAMETERS_SQL = 
				"INSERT INTO event_experience_params " +
			    "(event_experience_id, key, value) " +
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
					// And H2 Does support getGeneratedKeys() on batch inserts at all.
					PreparedStatement stmt = conn.prepareStatement(INSERT_EVENTS_SQL, Statement.RETURN_GENERATED_KEYS);

					for (VariantEventSupport event: events) {
						stmt.setString(1, event.getSession().getId());
						stmt.setTimestamp(2, new Timestamp(event.getCreateDate().getTime()));
						stmt.setString(3, event.getEventName());
						stmt.setString(4, event.getEventValue());

						stmt.addBatch();
					}
					
					// Send rows to the database.
					stmt.executeBatch();
					
					// Read sequence generated event IDs and add them to the event objects.
					// We'll need these ids when inserting into events_experiences.
					Iterator<VariantEventSupport> eventsIter = events.iterator();
					ResultSet gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (!eventsIter.hasNext()) 
							throw new VariantInternalException("Received more genereated keys than inserted event records.");
						
						VariantEventSupport event = eventsIter.next();
						event.setId(gennedKeys.getLong(1));
					}
					if (eventsIter.hasNext()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted event records.");
					
					stmt.close();
					
					//
					// 2. Insert into EVENTS_EXPERIENCES.
					//
					
					ArrayList<VariantEventExperienceSupport> eventExperiences = new ArrayList<VariantEventExperienceSupport>();
					
					stmt = conn.prepareStatement(INSERT_EVENT_EXPERIENCES_SQL, Statement.RETURN_GENERATED_KEYS);
					
					for (VariantEventSupport event: events) {
						for (VariantEventExperience ee: event.getEventExperiences()) {
							eventExperiences.add((VariantEventExperienceSupport)ee);
							stmt.setLong(1, ee.getEvent().getId());
							stmt.setString(2, ee.getExperience().getTest().getName());
							stmt.setString(3, ee.getExperience().getName());
							stmt.setBoolean(4, ee.getExperience().isControl());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();
					
					// Read sequence generated event IDs and add them to the event objects.
					// We'll need these ids when inserting into events_experiences.
					Iterator<VariantEventExperienceSupport> eventExperiencesIter = eventExperiences.iterator();
					gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (!eventExperiencesIter.hasNext()) 
							throw new VariantInternalException("Received more genereated keys than inserted event experience records.");
						
						VariantEventExperienceSupport ee = eventExperiencesIter.next();
						ee.setId(gennedKeys.getLong(1));
					}
					if (eventExperiencesIter.hasNext()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted event experience records.");
					
					stmt.close();

					//
					// 3. Insert into EVENT_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_PARAMETERS_SQL);
					
					for (VariantEventSupport event: events) {
						for (String key: event.getParameterKeys()) {
	
							stmt.setLong(1, event.getId());
							stmt.setString(2, key);
							stmt.setString(3, event.getParameter(key).toString());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();

					//
					// 4. Insert into EVENT_EXPERIENCE_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_EXPERIENCE_PARAMETERS_SQL);
					
					for (VariantEventExperienceSupport ee: eventExperiences) {
						for (String key: ee.getParameterKeys()) {
	
							stmt.setLong(1, ee.getId());
							stmt.setString(2, key);
							stmt.setString(3, ee.getParameter(key).toString());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();

					stmt.close();

					stmt.close();
				}
			}
		);
		
	}

	/**
	 * 
	 * @return
	 */
	public Vendor getVendor() {
		// Figure out the JDBC vendor, if we can.
		if (this instanceof com.variant.core.ext.EventPersisterPostgres) {
			return Vendor.POSTGRES;
		}
		else if (this instanceof com.variant.core.ext.EventPersisterH2) {
			return Vendor.H2;
		}
		else return null;
	}
	
	/**
	 * 
	 * @author noneofyourbusiness
	 *
	 */
	public static enum Vendor {
		POSTGRES,
		H2
	}
	


}
