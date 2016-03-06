package com.variant.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEventDecorator;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.Test;
import static com.variant.core.jdbc.JdbcService.Vendor;

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
	 * @throws Exception
	 */
	public abstract Connection getJdbcConnection() throws Exception;

	/**
	 * Implementations will know the vendor.
	 * @return
	 */
	public abstract Vendor getVendor();
	
	/**
	 * Persist a collection of events.
	 */
	@Override
	final public void persist(final Collection<VariantEventDecorator> events) throws Exception {
				
		final String INSERT_EVENTS_SQL = 
				"INSERT INTO events " +
			    "(id, session_id, created_on, event_name, event_value) " +
				(getVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('events_id_seq'), ?, ?, ?, ?)" :
				getVendor() == Vendor.H2 ?
						"VALUES (events_id_seq.NEXTVAL, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_VARIANTS_SQL = 
				"INSERT INTO event_variants " +
			    "(id, event_id, test_name, experience_name, is_experience_control) " +
				(getVendor() == Vendor.POSTGRES ?
						"VALUES (NEXTVAL('event_variants_id_seq'), ?, ?, ?, ?)" :
				getVendor() == Vendor.H2 ?
						"VALUES (event_variants_id_seq.NEXTVAL, ?, ?, ?, ?)" : "");

		final String INSERT_EVENT_PARAMETERS_SQL = 
				"INSERT INTO event_params " +
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

					for (VariantEventDecorator event: events) {
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
					long[] eventIds = new long[events.size()];
					int index = 0;
					ResultSet gennedKeys = stmt.getGeneratedKeys();
					while(gennedKeys.next()) {

						if (index == events.size()) 
							throw new VariantInternalException("Received more genereated keys than inserted event records.");
						
						eventIds[index++] = gennedKeys.getLong(1);
					}
					if (index < events.size()) 
						throw new VariantInternalException("Received fewer genereated keys than inserted event records.");

					stmt.close();

					//
					// 2. Insert into EVENT_PARAMETERS.
					//
					stmt = conn.prepareStatement(INSERT_EVENT_PARAMETERS_SQL);
					index = 0;
					for (VariantEventDecorator event: events) {
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
					for (VariantEventDecorator event: events) {
						long eventId = eventIds[index++];
						for (Test.Experience exp: event.getActiveExperiences()) {
							stmt.setLong(1, eventId);
							stmt.setString(2, exp.getTest().getName());
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
	
	/**
	 *  DEAD?
	 * @author Igor
	 *
	 *
	private static class EventWrapper {
		private VariantEvent event;
		private long id;
		private EventWrapper(VariantEvent event, long id) {
			this.event = event;
			this.id = id;
		}
	}
	*/
}
