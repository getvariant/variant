package com.variant.server.dispatch

import java.io.OutputStream
import java.util.Date
import com.variant.server.SessionCache
import com.variant.server.boot.UserError
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
import com.variant.server.RemoteEvent
import net.liftweb.http.NoContentResponse
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json.JsonAST.JArray
import org.apache.http.HttpStatus
import net.liftweb.http.PlainTextResponse

/**
 * @author Igor
 *
 */
object Dispatcher extends RestHelper with LazyLogging {

   // Remember about the prefix helper!

   serve {
      case "event" :: Nil Options req => options(req)
      case "session" :: id :: Nil JsonGet _ => getSession(id)
      case "session" :: id :: Nil JsonPut json -> req => updateSession(id, req)
      case "event" :: Nil JsonPost json -> req => postEvent(json)
   }

   def options(req: Req): LiftResponse = {
      val headers = S.getResponseHeaders(List(
         ("Access-Control-Allow-Methods", "*"),
         ("Access-Control-Allow-Headers", "origin, content-type, accept")))
      PlainTextResponse("", headers, HttpStatus.SC_OK)
   }
   
   /**
    * GET /session/{id}
    *
    * Get a new session. In the future, when schema is processes on the server,
    * we'll be able to also create, but for now we return null if did not exist
    * and let the client create and POST it back here.
    * 
    * @since 0.6
    */
   def getSession(id: String): LiftResponse = {

      var result = SessionCache.get(id)
      if (result == null) {
         logger.trace("No session found for ID " + id)         
         NoContentResponse()
      }
      else {
         logger.trace("Session found for ID " + id)
         OutputStreamResponse(out => {out.write(result.getJson)})
      }
   }

   /**
    * PUT /session/{id}
    * Create or update an existing session.
    * Do not attempt to parse the payload. 
    */
   def updateSession(id:String, req: Req): Box[LiftResponse] = {      
      if (!req.body.isDefined || req.body.openOrThrowException("Unexpectged null request body").length == 0) 
         return UserError.errors(UserError.EmptyBody).toFailure()
      
      if (SessionCache.put(id, req.body.openOrThrowException("Unexpectged null request body")) == null)
         logger.trace("Saved session ID " + id)
      else
         logger.trace("Updated session ID " + id)
      
      val response = OkResponse()

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
            case "PARAMETERS" => {params = field.extract[Option[JObject]]}
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
         for ((k,v) <- params.get.values) {
            if (!v.isInstanceOf[String]) return UserError.errors(UserError.PropertyNotAString).toFailure(k)
            remoteEvent.setParameter(k, v.asInstanceOf[String])
         }
      }

      // 403 if no session
      val ssnCacheEntry = SessionCache.get(sid.get)
      if (ssnCacheEntry == null) return  UserError.errors(UserError.SessionExpired).toFailure()

      // We have the session. Must have no recent state request.
      if (ssnCacheEntry.getSession.getStateRequest == null) 
         return UserError.errors(UserError.UnknownState).toFailure()
 
      // Trigger event
      ssnCacheEntry.getSession.triggerEvent(remoteEvent)
      
      Full(OkResponse())
   }

   def createEvent(req: Req): String = "event"

}
