package com.variant.client.test.util.event;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * 
 */
public class TraceEventReader {
   
	private final static String SELECT_EVENTS_SQL = 
			"SELECT e.id, e.session_id, e.created_on, e.event_name, p.name, p.value " +
			"FROM events e LEFT OUTER JOIN event_attributes p ON e.id = p.event_id";

	private final static String SELECT_EVENT_EXPERIENCES_SQL =
			"SELECT id, event_id, variation_name, experience_name, is_control " +
			"FROM event_experiences";

	private final Connection conn; //  = eventWriter.flusher.asInstanceOf[EventFlusherJdbc].getJdbcConnection

	/**
	 * 
	 */
	public TraceEventReader() throws SQLException {
		Properties props = new Properties();
		props.setProperty("user", "variant");
		props.setProperty("password", "variant");
		conn = DriverManager.getConnection("jdbc:postgresql://localhost/variant", props);		
	}
	
   /**
	 * Read events as a list. The result list is sorted by event ID, i.e. in chronological order.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public List<TraceEventFromDatabase> read(Predicate<? super TraceEventFromDatabase> p) throws SQLException {

		Collection<TraceEventFromDatabase> result = JdbcAdapter.executeQuery (
				
				conn, 
				new JdbcAdapter.QueryOperation<Collection<TraceEventFromDatabase>>() {

				@Override
				public Collection<TraceEventFromDatabase> execute(Connection conn) throws SQLException {
					
					// Read events
					Statement stmt = conn.createStatement();
					ResultSet eventsRresulSet = stmt.executeQuery(SELECT_EVENTS_SQL);

					// Keep items in a map keyed by event ID for easy access.
					Map<Long, TraceEventFromDatabase> eventMap = new HashMap<Long, TraceEventFromDatabase>();
					
					while (eventsRresulSet.next()) {
						Long id = eventsRresulSet.getLong(1);
						TraceEventFromDatabase event = eventMap.get(id);
						if (event == null) {
							event = new TraceEventFromDatabase(
							   id, 
							   eventsRresulSet.getString(2).trim(),  // Fixed width in DB.
							   eventsRresulSet.getTimestamp(3),
							   eventsRresulSet.getString(4));
							eventMap.put(id, event);
						}
						String key = eventsRresulSet.getString(5);
						if (key != null) event.attributes.put(key, eventsRresulSet.getString(6));
					}
					
					// Read event_experiences
					// Keep items in a map (keyed by event ID) of maps (keyed by by event_experience ID) for easy access.
					Map<Long,Map<Long, EventExperienceFromDatabase>> outerMap = new HashMap<Long, Map<Long, EventExperienceFromDatabase>>();
					ResultSet eventExperiencesResultSet = stmt.executeQuery(SELECT_EVENT_EXPERIENCES_SQL);
					
					while (eventExperiencesResultSet.next()) {
						
						Long eventId = eventExperiencesResultSet.getLong(2);
						Map<Long, EventExperienceFromDatabase> innerMap = outerMap.get(eventId);
						if (innerMap == null) {
							innerMap = new HashMap<Long, EventExperienceFromDatabase>();
							outerMap.put(eventId, innerMap);
						}
						
						Long eeId = eventExperiencesResultSet.getLong(1);
						EventExperienceFromDatabase ee = innerMap.get(eeId);
						if (ee == null) {
							ee = new EventExperienceFromDatabase(
   						   eeId,
							   eventId,
							   eventExperiencesResultSet.getString(3),
							   eventExperiencesResultSet.getString(4),
							   eventExperiencesResultSet.getBoolean(5));
							innerMap.put(eeId, ee);
						}
					}
					
					// Attach event_experiences to events
					for (Map.Entry<Long, Map<Long, EventExperienceFromDatabase>> e: outerMap.entrySet()) {
						Long eventId = e.getKey();
						Map<Long, EventExperienceFromDatabase> inner = e.getValue();
						TraceEventFromDatabase event = eventMap.get(eventId);
						for (Map.Entry<Long, EventExperienceFromDatabase> ee: inner.entrySet()) {
							event.eventExperiences.add(ee.getValue());
						}
					}
					return eventMap.values();
				}
			}
		);
		
		return result
				.stream()
				.filter(p)
				.sorted((e1,e2) -> e1.id.compareTo(e2.id))
				.collect(Collectors.<TraceEventFromDatabase>toList());
	}
}
