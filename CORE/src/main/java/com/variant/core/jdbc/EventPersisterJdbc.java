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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.VariantEventVariant;
import com.variant.core.event.impl.StateServeEventVariant;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(EventPersisterJdbc.class);
			
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
	final public void persist(final Collection<VariantEvent> events) throws Exception {

		if (getVendor() == Vendor.H2) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(getClass().getSimpleName() + ".persist() quit becuase H2 does not support generated key retrieval on batch inserts");
			}
			return;
		}
		
				final String INSERT_EVENTS_SQL = 
				"INSERT INTO events " +
			    "(id, session_id, created_on, event_name, event_value) " +
				(getVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('events_id_seq'), ?, ?, ?, ?)" :
				getVendor() == Vendor.H2 ?
						"VALUES (events_id_seq.NEXTVAL, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_VARIANTS_SQL = 
				"INSERT INTO event_variants " +
			    "(id, event_id, test_name, experience_name, is_experience_control, is_state_nonvariant) " +
				(getVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('event_variants_id_seq'), ?, ?, ?, ?, ?)" :
				getVendor() == Vendor.H2 ?
						"VALUES (event_variants_id_seq.NEXTVAL, ?, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_PARAMETERS_SQL = 
				"INSERT INTO event_params " +
			    "(event_id, key, value) " +
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
					// https://github.com/h2database/h2database/issues/156
					PreparedStatement stmt = conn.prepareStatement(INSERT_EVENTS_SQL, Statement.RETURN_GENERATED_KEYS);

					for (VariantEvent event: events) {
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
					Iterator<VariantEvent> eventsIter = events.iterator();
					ResultSet gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (!eventsIter.hasNext()) 
							throw new VariantInternalException("Received more genereated keys than inserted event records.");
						
						VariantEvent event = eventsIter.next();
						event.setId(gennedKeys.getLong(1));
					}
					if (eventsIter.hasNext()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted event records.");
					
					stmt.close();
					
					//
					// 2. Insert into EVENTS_EXPERIENCES.
					//
					
					ArrayList<StateServeEventVariant> eventVariants = new ArrayList<StateServeEventVariant>();
					
					stmt = conn.prepareStatement(INSERT_EVENT_VARIANTS_SQL, Statement.RETURN_GENERATED_KEYS);
					
					for (VariantEvent event: events) {
						for (VariantEventVariant ee: event.getEventVariants()) {
							eventVariants.add((StateServeEventVariant)ee);
							stmt.setLong(1, ee.getEvent().getId());
							stmt.setString(2, ee.getExperience().getTest().getName());
							stmt.setString(3, ee.getExperience().getName());
							stmt.setBoolean(4, ee.isExperienceControl());
							stmt.setBoolean(5, ee.isStateNonvariant());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();
					
					// Read sequence generated event IDs and add them to the event objects.
					// We'll need these ids when inserting into events_experiences.
					Iterator<StateServeEventVariant> eventExperiencesIter = eventVariants.iterator();
					gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (!eventExperiencesIter.hasNext()) 
							throw new VariantInternalException("Received more genereated keys than inserted event experience records.");
						
						StateServeEventVariant ee = eventExperiencesIter.next();
						ee.setId(gennedKeys.getLong(1));
					}
					if (eventExperiencesIter.hasNext()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted event experience records.");
					
					stmt.close();

					//
					// 3. Insert into EVENT_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_PARAMETERS_SQL);
					
					for (VariantEvent event: events) {
						for (String key: event.getParameterKeys()) {
	
							stmt.setLong(1, event.getId());
							stmt.setString(2, key);
							stmt.setString(3, event.getParameter(key).toString());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();
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
