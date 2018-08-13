package com.variant.server.test.util;

import play.api.libs.json._

object EventExperienceFromDatabase {
   
   /**
    * Implicit writes converter from EventExperienceFromDatabase to JsValue
    */
   implicit val writes = new Writes[EventExperienceFromDatabase] {
     
      def writes(exp: EventExperienceFromDatabase) = 
         Json.obj(
            "id" -> exp.id,
            "eventId" -> exp.eventId,
            "testName" -> exp.testName,
            "expName" -> exp.experienceName,
            "isControl" -> exp.isControl
         )
}
}
class EventExperienceFromDatabase (
		val id: Long,
		val eventId: Long,
		val testName: String,
		val experienceName: String,
		val isControl: Boolean
		) {
   
   
  
}
