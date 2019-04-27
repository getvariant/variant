package com.variant.server.test.util

import java.time.Instant
import scala.collection.mutable
import play.api.libs.json._
import java.time.format.DateTimeFormatter

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;

class TraceEventFromDatabase (
		val id: Long,
		val sessionId: String,
		val createdOn: Instant,
		val name: String
	) {

	val attributes = mutable.Map[String,String]()
	val eventExperiences = mutable.Set[EventExperienceFromDatabase]()

    	
	/** ********************
	 * Predicate base selection.
	 * @param p
	 * @return
	 * Breaks compilation because we no longer have apache commons Predicate.
	 * If this is actually needed, replace with Scala class make this method take a function param.
	public Collection<EventExperienceFromDatabase> getEventExperiences(Predicate<EventExperienceFromDatabase> p) { 
		HashSet<EventExperienceFromDatabase> result = new HashSet<EventExperienceFromDatabase>();
		for (EventExperienceFromDatabase ee: eventExperiences) if (p.evaluate(ee)) result.add(ee);
		return result; 	
	}
    ***********************/
	
	override def toString(): String = {
		Json.prettyPrint( 
   	   Json.obj(
   		  "id" -> id,
   		  "sessionId" -> sessionId,
   		  "createdOn" -> DateTimeFormatter.ISO_INSTANT.format(createdOn),
   		  "name" -> name,
   		  "attrs" -> attributes,
   		  "expList" -> eventExperiences
   		))
	}
}
