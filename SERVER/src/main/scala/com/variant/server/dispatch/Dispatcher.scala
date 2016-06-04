package com.variant.server.dispatch

import com.variant.server.lift.Boot
import net.liftweb.http.Req
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JValue
import com.variant.server.RemoteEvent
import net.liftweb.common._
import java.util.Date
import com.variant.server.config.UserError
import net.liftweb.json.JsonAST._
import scala.util.control.NonFatal

/**
 * @author Igor
 *
 */
object Dispatcher extends RestHelper {

   // Remember about the prefix helper!

   serve {
      case "hello" :: "world" :: _ Get _ => <b>Hello World</b>
      case "event" :: Nil JsonPost json -> req => postEvent(json)
   }

   /**
    * POST /event
    *
    * Add a new event to the current session.
    */
   def postEvent(jsonData: JValue): Box[JValue] = {

      var name: Option[String] = Option(null)
      var value: Option[String] = Option(null)
      var createDate: Option[Long] = Option(System.currentTimeMillis())
      var params: Option[JObject] = Option(null)

      for (elem <- jsonData.children) {
         val field = elem.asInstanceOf[JField]
         field.name.toUpperCase() match {
            case "NAME" => name = field.extract[Option[String]]
            case "VALUE" => value = field.extract[Option[String]]
            case "CREATEDATE" => 
               try {
                  createDate = field.extract[Option[Long]]
               } catch {
                  case _ => return UserError.errors(UserError.InvalidDate).toFailure(field.name)
               }
            case "PARAMETERS" => params = field.extract[Option[JObject]]
            case _ => return UserError.errors(UserError.UnsupportedProperty).toFailure(field.name)
         }
      }

      if (!name.isDefined) return UserError.errors(UserError.MissingProperty).toFailure("name")
      if (!value.isDefined) return UserError.errors(UserError.MissingProperty).toFailure("value")

      val remoteEvent = new RemoteEvent(name.get, value.get);

      if (createDate.isDefined) {
         try {
            remoteEvent.setCreateDate(new Date(createDate.get))
         } catch {
            case _: NumberFormatException => return UserError.errors(UserError.InvalidDate).toFailure("....")
         }
      }

      if (params.isDefined) {
         for ((k, v) <- params.get.values) {
            if (!v.isInstanceOf[String]) return UserError.errors(UserError.ParamNotAString).toFailure(k)
            remoteEvent.setParameter(k, v.asInstanceOf[String])
         }
      }

      Full(jsonData)
   }

   def createEvent(req: Req): String = "event"

}
