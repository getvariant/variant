package com.variant.server.test.util

import java.sql.Connection
import java.sql.ResultSet
import java.util.Collection
import java.util.HashMap
import java.util.Map
import scala.collection.JavaConversions._
import org.junit.runners.model.Statement
import com.variant.server.impl.TraceEventWriter
import com.variant.core.util.JdbcAdapter


/**
 * Read events written by a JDBC event flusher.
 * Tests only
 * 
 * @author Igor
 *
 */
object TraceEventReader {
   def apply(eventWriter: TraceEventWriter) = new TraceEventReader(eventWriter)
}

/**
 * 
 */
class TraceEventReader (eventWriter: TraceEventWriter) {
   
   val jdbcConnection = new JdbcService(eventWriter).getConnection

   /**
	 * Read events as a collection
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	def read(p: TraceEventFromDatabase => Boolean = (_ => true)) = {

		val SELECT_EVENTS_SQL = 
				"SELECT e.id, e.session_id, e.created_on, e.event_name, p.name, p.value " +
				"FROM events e LEFT OUTER JOIN event_attributes p ON e.id = p.event_id";

		val SELECT_EVENT_EXPERIENCES_SQL =
				"SELECT id, event_id, variation_name, experience_name, is_control " +
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
							event = new TraceEventFromDatabase(
							   id, 
							   eventsRresulSet.getString(2).trim(),  // Fixed width in DB.
							   eventsRresulSet.getTimestamp(3),
							   eventsRresulSet.getString(4))
							eventMap.put(id, event);
						}
						val key = eventsRresulSet.getString(5);
						if (key != null) event.attributes.put(key, eventsRresulSet.getString(6));
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
							ee = new EventExperienceFromDatabase(
   						   eeId,
							   eventId,
							   eventExperiencesResultSet.getString(3),
							   eventExperiencesResultSet.getString(4),
							   eventExperiencesResultSet.getBoolean(5))
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
