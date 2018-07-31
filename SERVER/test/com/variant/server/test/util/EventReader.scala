package com.variant.server.test.util

import com.variant.server.jdbc.JdbcService
import com.variant.server.jdbc.JdbcAdapter
import java.util.Collection
import java.sql.Connection
import org.junit.runners.model.Statement
import java.sql.ResultSet
import java.util.HashMap
import java.util.HashSet
import java.util.Map
import scala.collection.JavaConversions._
import com.variant.server.event.EventWriter
import com.variant.server.jdbc.EventFlusherJdbc


/**
 * Read events written by a JDBC event flusher.
 * Tests only
 * 
 * @author Igor
 *
 */
object EventReader {
   def apply(eventWriter: EventWriter) = new EventReader(eventWriter)
}

/**
 * 
 */
class EventReader (eventWriter: EventWriter) {
   
   val jdbcConnection = eventWriter.flusher.asInstanceOf[EventFlusherJdbc].getJdbcConnection

   /**
	 * Read events as a collection
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	def read(p: TraceEventFromDatabase => Boolean = (_ => true)) = {

		val SELECT_EVENTS_SQL = 
				"SELECT e.id, e.session_id, e.created_on, e.event_name, e.event_value, p.key, p.value " +
				"FROM events e LEFT OUTER JOIN event_params p ON e.id = p.event_id";

		val SELECT_EVENT_EXPERIENCES_SQL =
				"SELECT id, event_id, test_name, experience_name, is_control " +
				"FROM event_experiences";
		
		JdbcAdapter.executeQuery(
			jdbcConnection, 
			new JdbcAdapter.QueryOperation[Collection[TraceEventFromDatabase]]() {

				override def execute(conn: Connection) = {
					
					// Read events
					val stmt = conn.createStatement();
					val eventsRresulSet = stmt.executeQuery(SELECT_EVENTS_SQL);

					// Keep items in a map keyed by event ID for easy access.
					val eventMap = new HashMap[Long, TraceEventFromDatabase]();
					
					while (eventsRresulSet.next()) {
						val id = eventsRresulSet.getLong(1);
						var event = eventMap.get(id);
						if (event == null) {
							event = new TraceEventFromDatabase();
							event.id = id;
							event.sessionId = eventsRresulSet.getString(2).trim(); // Fixed width in DB.
							event.createdOn = eventsRresulSet.getTimestamp(3);
							event.name = eventsRresulSet.getString(4);
							event.value = eventsRresulSet.getString(5);
							event.eventExperiences = new HashSet[EventExperienceFromDatabase]();
							event.params = new HashMap[String,String]();
							eventMap.put(id, event);
						}
						val key = eventsRresulSet.getString(6);
						if (key != null) event.params.put(key, eventsRresulSet.getString(7));
					}
					
					// Read event_experiences
					// Keep items in a map (keyed by event ID) of maps (keyed by by event_experience ID) for easy access.
					val outerMap = new HashMap[Long, Map[Long, EventExperienceFromDatabase]]();
					val eventExperiencesResultSet = stmt.executeQuery(SELECT_EVENT_EXPERIENCES_SQL);
					
					while (eventExperiencesResultSet.next()) {
						
						val eventId = eventExperiencesResultSet.getLong(2);
						var innerMap = outerMap.get(eventId);
						if (innerMap == null) {
							innerMap = new HashMap[Long, EventExperienceFromDatabase]();
							outerMap.put(eventId, innerMap);
						}
						
						val eeId = eventExperiencesResultSet.getLong(1);
						var ee = innerMap.get(eeId);
						if (ee == null) {
							ee = new EventExperienceFromDatabase();
							ee.id = eeId;
							ee.eventId = eventId;
							ee.testName = eventExperiencesResultSet.getString(3);
							ee.experienceName = eventExperiencesResultSet.getString(4);
							ee.isControl = eventExperiencesResultSet.getBoolean(5);
							innerMap.put(eeId, ee);
						}
					}
					
					// Attach event_experiences to events
					for ((eventId, inner) <- outerMap) {
						val event = eventMap.get(eventId)
						for ((eeId, eventExperience) <- inner) {
							event.eventExperiences.add(eventExperience)
						}
					}
					eventMap.values()
				}
			}
		).filter(p)
	}
}
