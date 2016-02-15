package com.variant.server.dispatch

import java.io.OutputStream
import java.util.Date
import com.variant.core.session.VariantSessionImpl
import com.variant.server.RemoteEvent
import com.variant.server.SessionCache
import com.variant.server.config.UserError
import net.liftweb.common.Box
import net.liftweb.common.EmptyBox
import net.liftweb.common.Full
import net.liftweb.http.OutputStreamResponse
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JField
import net.liftweb.json.JObject
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.parse
import net.liftweb.http.GetRequest
import net.liftweb.http.LiftResponse
import net.liftweb.common.Empty
import net.liftweb.http.OkResponse

/**
 * @author Igor
 *
 */
object Dispatcher extends RestHelper {

   // Remember about the prefix helper!

   serve {
      case "session" :: id :: Nil JsonGet _ => getOrCreateSession(id)
      case "session" :: id :: Nil JsonPut json -> req => updateSession(id, req)
      case "event" :: Nil JsonPost json -> req => postEvent(json)
   }

   /**
    * GET /session/{id}
    *
    * Get or create a new session.
    * If created, send only the ID, and the caller will know to re-instantiate a session object.
    */
   def getOrCreateSession(id: String): LiftResponse = {

      var result = SessionCache.get(id)
      
      if (result == null) {
         // New or expired: recreate with nothing but session id
         result = SessionCache.put(id, new VariantSessionImpl(id).toJson())
      }

      //OutputStreamResponse(out => {out.write(result.getJson.toCharArray.map(_.toByte)); out.flush(); out.close()})
      OutputStreamResponse(out => {out.write(result.getJson)})
   }

   /**
    * PUT /session/{id}
    * Update an existing session. Recreate, if already removed from the cache.
    */
   def updateSession(id:String, req: Req): LiftResponse = {
      
      if (!req.body.isDefined) 
      SessionCache.put(id, req.body.openOr(null))
      println("***** " + new String(req.body.openOr(null)))
      OkResponse()
   }

   /**
    * POST /event
    *
    * Add a new event to the current session.
    */
   def postEvent(jsonData: JValue): Box[JValue] = {

      // Decline request if no session.
      println(S.findCookie("puke"));
      //val variant = Variant.Factory.getInstance;
      //val variantSession = variant.getSession(x$1);

      // Parse the input and construct the remote event.
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
                  case _: Exception => return UserError.errors(UserError.InvalidDate).toFailure(field.name)
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

      // We have the 
      Full(jsonData)
   }

   def createEvent(req: Req): String = "event"

}
