package com.variant.core.test.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections4.Predicate;

import com.variant.core.impl.VariantCore;
import com.variant.core.jdbc.JdbcAdapter;
import com.variant.core.jdbc.JdbcService;

/**
 * Read events from a JDBC event persister.
 * Tests only
 * 
 * @author Igor
 *
 */
public class EventReader {

	private JdbcService jdbcService = null;

	/**
	 * 
	 * @param variant
	 */
	public EventReader(VariantCore variant) {
		jdbcService = new JdbcService(variant);
	}
	
	/**
	 * Read events as a collection
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Collection<VariantEventFromDatabase> readEvents(final Predicate<VariantEventFromDatabase> filter) 
		throws Exception {
		
		final String SELECT_EVENTS_SQL = 
				"SELECT e.id, e.session_id, e.created_on, e.event_name, e.event_value, p.key, p.value " +
				"FROM events e LEFT OUTER JOIN event_params p ON e.id = p.event_id";

		final String SELECT_EVENT_EXPERIENCES_SQL =
				"SELECT id, event_id, test_name, experience_name, is_control " +
				"FROM event_experiences";
		
		return JdbcAdapter.executeQuery(
			jdbcService.getConnection(), 
			new JdbcAdapter.QueryOperation<Collection<VariantEventFromDatabase>>() {

				@Override
				public Collection<VariantEventFromDatabase> execute(Connection conn) throws SQLException {
					
					// Read events
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(SELECT_EVENTS_SQL);
					
					// Keep items in a map keyed by event ID for easy access.
					HashMap<Long, VariantEventFromDatabase> eventMap = new HashMap<Long, VariantEventFromDatabase>();
					
					while (rs.next()) {
						Long id = rs.getLong(1);
						VariantEventFromDatabase event = eventMap.get(id);
						if (event == null) {
							event = new VariantEventFromDatabase();
							event.id = id;
							event.sessionId = rs.getString(2).trim(); // Fixed width in DB.
							event.createdOn = rs.getDate(3);
							event.name = rs.getString(4);
							event.value = rs.getString(5);
							event.eventExperiences = new HashSet<EventExperienceFromDatabase>();
							event.params = new HashMap<String,String>();
							eventMap.put(id, event);
						}
						String key = rs.getString(6);
						if (key != null) event.params.put(key, rs.getString(7));
					}
					
					// Read event_experiences
					// Keep items in a map (keyed by event ID) of maps (keyed by by event_experience ID) for easy access.
					HashMap<Long, Map<Long, EventExperienceFromDatabase>> outerMap = new HashMap<Long, Map<Long, EventExperienceFromDatabase>>();
					rs = stmt.executeQuery(SELECT_EVENT_EXPERIENCES_SQL);
					
					while (rs.next()) {
						
						Long eventId = rs.getLong(2);
						Map<Long, EventExperienceFromDatabase> innerMap = outerMap.get(eventId);
						if (innerMap == null) {
							innerMap = new HashMap<Long, EventExperienceFromDatabase>();
							outerMap.put(eventId, innerMap);
						}
						
						long evId = rs.getLong(1);
						EventExperienceFromDatabase ev = innerMap.get(evId);
						if (ev == null) {
							ev = new EventExperienceFromDatabase();
							ev.id = evId;
							ev.eventId = eventId;
							ev.testName = rs.getString(3);
							ev.experienceName = rs.getString(4);
							ev.isControl = rs.getBoolean(5);
							innerMap.put(evId, ev);
						}
						
					}
					
					// Attach event_experiences to events
					for (Map.Entry<Long, Map<Long, EventExperienceFromDatabase>> outerEntry: outerMap.entrySet()) {
						VariantEventFromDatabase event = eventMap.get(outerEntry.getKey());
						for (Map.Entry<Long, EventExperienceFromDatabase> innerEntry: outerEntry.getValue().entrySet()) {
							event.eventExperiences.add(innerEntry.getValue());
						}
					}
					
					// Apply filter
					Iterator<VariantEventFromDatabase> result = eventMap.values().iterator();
					while(result.hasNext()) {
						VariantEventFromDatabase e = result.next();
						if (!filter.evaluate(e)) result.remove();
					}
					
					return eventMap.values();
				}
			}
		);
	}			
	
	/**
	 * Read events as a collection
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Collection<VariantEventFromDatabase> readEvents() throws Exception {

		Predicate<VariantEventFromDatabase> noFilter = new Predicate<VariantEventFromDatabase>() {

			@Override
			public boolean evaluate(VariantEventFromDatabase t) {
				return true;
			}
		};
		return readEvents(noFilter);
	}
}
