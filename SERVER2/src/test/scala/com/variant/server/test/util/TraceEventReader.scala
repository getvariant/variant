package com.variant.server.test.util

import java.sql.Connection
import java.sql.ResultSet
import java.util.Collection
import java.util.HashMap
import java.util.Map
import scala.collection.JavaConverters._
import org.junit.runners.model.Statement
import com.variant.server.impl.TraceEventWriter
import com.variant.core.util.JdbcAdapter
import java.time.Instant

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
class TraceEventReader(eventWriter: TraceEventWriter) {

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
         "SELECT event_id, variation_name, experience_name, is_control " +
            "FROM event_experiences";

      JdbcAdapter.executeQuery(
         jdbcConnection,
         new JdbcAdapter.QueryOperation[Collection[TraceEventFromDatabase]]() {

            override def execute(conn: Connection) = {

               // Read events and event attributes
               val stmt = conn.createStatement();
               val eventsRresulSet = stmt.executeQuery(SELECT_EVENTS_SQL);

               // Keep items in a map keyed by event ID for easy access.
               val eventMap = new HashMap[String, TraceEventFromDatabase]();

               while (eventsRresulSet.next()) {
                  val id = eventsRresulSet.getString(1);
                  var event = eventMap.get(id);
                  if (event == null) {
                     event = new TraceEventFromDatabase(
                        id,
                        eventsRresulSet.getString(2).trim(), // Fixed width in DB.
                        eventsRresulSet.getTimestamp(3).toInstant,
                        eventsRresulSet.getString(4))
                     eventMap.put(id, event);
                  }
                  val key = eventsRresulSet.getString(5);
                  if (key != null) event.attributes.put(key, eventsRresulSet.getString(6));
               }

               // Read event_experiences and add them to events.
               val eventExperiencesResultSet = stmt.executeQuery(SELECT_EVENT_EXPERIENCES_SQL);

               while (eventExperiencesResultSet.next()) {

                  val eventId = eventExperiencesResultSet.getString(1);
                  val ee = new EventExperienceFromDatabase(
                     eventId,
                     eventExperiencesResultSet.getString(2),
                     eventExperiencesResultSet.getString(3),
                     eventExperiencesResultSet.getBoolean(4))

                  eventMap.get(eventId).eventExperiences.add(ee)

               }

               eventMap.values()
            }
         }).asScala.filter(p)
   }
}
