package com.variant.server.test.util

import java.time.Instant
import scala.collection.mutable
import play.api.libs.json._
import java.time.format.DateTimeFormatter

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;

class TraceEventFromDatabase (
		val id: String,
		val sessionId: String,
		val createdOn: Instant,
		val name: String
	) {

	val attributes = mutable.Map[String,String]()
	val eventExperiences = mutable.Set[EventExperienceFromDatabase]()
	
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
