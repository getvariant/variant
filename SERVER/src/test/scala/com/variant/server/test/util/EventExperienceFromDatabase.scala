package com.variant.server.test.util;

import play.api.libs.json._

object EventExperienceFromDatabase {

   /**
    * Implicit writes converter from EventExperienceFromDatabase to JsValue
    */
   implicit val writes = new Writes[EventExperienceFromDatabase] {

      def writes(exp: EventExperienceFromDatabase) =
         Json.obj(
            "eventId" -> exp.eventId,
            "varName" -> exp.variationName,
            "expName" -> exp.experienceName,
            "isControl" -> exp.isControl)
   }
}
class EventExperienceFromDatabase(
   val eventId: String,
   val variationName: String,
   val experienceName: String,
   val isControl: Boolean) {

   override def toString() = Json.toJson(this).toString()
}
