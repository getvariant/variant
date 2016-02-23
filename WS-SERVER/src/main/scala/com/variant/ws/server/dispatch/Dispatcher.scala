package com.variant.ws.server.dispatch

import java.io.OutputStream
import java.util.Date

import com.variant.core.session.VariantSessionImpl
import com.variant.server.RemoteEvent
import com.variant.ws.server.SessionCache;
import com.variant.ws.server.config.UserError

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
import net.liftweb.http.OkResponse
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
      // If new or expired, recreate with nothing but session id
      if (result == null) result = SessionCache.put(id, new VariantSessionImpl(id).toJson())
      OutputStreamResponse(out => {out.write(result.getJson)})
   }

   /**
    * PUT /session/{id}
    * Update an existing session. Recreate, if already removed from the cache.
    */
   def updateSession(id:String, req: Req): Box[LiftResponse] = {      
      if (!req.body.isDefined || req.body.openOrThrowException("Unexpectged null request body").length == 0) 
         return UserError.errors(UserError.EmptyBody).toFailure()
      SessionCache.put(id, req.body.openOrThrowException("Unexpectged null request body"))
      Full(OkResponse())
   }

   /**
    * POST /event
    *
    * Add a new event to the current session.
    */
   def postEvent(jsonData: JValue): Box[LiftResponse] = {

      // Parse the input and construct the remote event.
      var sid: Option[String] = Option(null)
      var name: Option[String] = Option(null)
      var value: Option[String] = Option(null)
      var createDate: Option[Long] = Option(System.currentTimeMillis())
      var params: Option[JObject] = Option(null)

      for (elem <- jsonData.children) {
         val field = elem.asInstanceOf[JField]
         field.name.toUpperCase() match {
            case "SID" => sid = field.extract[Option[String]]
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
   
      // 400 if no required fields 
      if (!sid.isDefined)   return UserError.errors(UserError.MissingProperty).toFailure("sid")
      if (!name.isDefined)  return UserError.errors(UserError.MissingProperty).toFailure("name")
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
            if (!v.isInstanceOf[String]) return UserError.errors(UserError.PropertyNotAString).toFailure(k)
            remoteEvent.setParameter(k, v.asInstanceOf[String])
         }
      }

      // 403 if no session
      val ssnCacheEntry = SessionCache.get(sid.get)
      if (ssnCacheEntry == null) return  UserError.errors(UserError.SessionExpired).toFailure()

      // We have the session. Must have recent state request.
      if (ssnCacheEntry.getSession.getStateRequest == null) 
         return UserError.errors(UserError.UnknownState).toFailure()
 
      // Trigger event
      ssnCacheEntry.getSession.triggerEvent(remoteEvent)
      
      Full(OkResponse())
   }

   def createEvent(req: Req): String = "event"

}
