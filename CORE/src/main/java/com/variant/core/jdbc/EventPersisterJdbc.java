package com.variant.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.VariantEventSupport;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

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
	final public void persist(final Collection<Pair<VariantEvent, Collection<Test.Experience>>> eventPairs) throws Exception {

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
					
					// H2 does not support getGeneratedKeys() on batch inserts.
					// https://github.com/h2database/h2database/issues/156
					PreparedStatement stmt = conn.prepareStatement(INSERT_EVENTS_SQL, Statement.RETURN_GENERATED_KEYS);

					for (Pair<VariantEvent, Collection<Test.Experience>> pair: eventPairs) {
						VariantEvent event = pair.arg1();
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
					long[] eventIds = new long[eventPairs.size()];
					int index = 0;
					ResultSet gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (index == eventPairs.size()) 
							throw new VariantInternalException("Received more genereated keys than inserted event records.");
						
						eventIds[index++] = gennedKeys.getLong(1);
					}
					if (index < eventPairs.size()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted event records.");

					stmt.close();

					//
					// 2. Insert into EVENT_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_PARAMETERS_SQL);
					index = 0;
					for (Pair<VariantEvent, Collection<Test.Experience>> pair: eventPairs) {
						VariantEvent event = pair.arg1();
						long eventId = eventIds[index++];
						for (Map.Entry<String, Object> param: event.getParameterMap().entrySet()) {

							stmt.setLong(1, eventId);
							stmt.setString(2, param.getKey());
							stmt.setString(3, param.getValue().toString());
						
							stmt.addBatch();
						}
					}
					
					stmt.executeBatch();
					stmt.close();
					
					//
					// 3. Insert into EVENT_VARIANTS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_VARIANTS_SQL);
					index = 0;
					for (Pair<VariantEvent, Collection<Test.Experience>> pair: eventPairs) {
						VariantEvent event = pair.arg1();
						long eventId = eventIds[index++];
						Collection<Test.Experience> activeExperiences = pair.arg2();
						for (Test.Experience exp: activeExperiences) {
							stmt.setLong(1, eventId);
							stmt.setString(2, exp.getTest().getName());
							stmt.setString(3, exp.getName());
							stmt.setBoolean(4, exp.isControl());
							State state = ((VariantEventSupport) event).getStateRequest().getState();
							stmt.setBoolean(5, state.isInstrumentedBy(exp.getTest()) && state.isNonvariantIn(exp.getTest()));
						
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
	 * @author Igor
	 *
	 */
	public static enum Vendor {
		POSTGRES,
		H2
	}
	
	/**
	 * 
	 * @author Igor
	 *
	 */
	private static class EventWrapper {
		private VariantEvent event;
		private long id;
		private EventWrapper(VariantEvent event, long id) {
			this.event = event;
			this.id = id;
		}
	}

}
