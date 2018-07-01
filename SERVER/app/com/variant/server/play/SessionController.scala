package com.variant.server.play

import com.variant.core.impl.ServerError.EmptyBody
import com.variant.core.session.CoreSession
import com.variant.core.impl.ServerError._
import com.variant.server.api.ServerException
import com.variant.server.boot.SessionStore
import com.variant.server.impl.SessionImpl
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.ControllerComponents
import com.variant.core.impl.ServerError
import com.variant.server.boot.VariantServer
import com.variant.server.api.Session
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.schema.SchemaGen

class SessionController @Inject() (
      val action: VariantAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
   
   private val logger = Logger(this.getClass)	

   /**
    * Get a session by ID, if exists in any of the given schema's generations.
    */
   def getSession(schemaName:String, sid:String) = action { req =>
     
      val ssn = server.ssnStore.getOrBust(sid)
      
      // Must match the schema name.
      if (ssn.schemaGen.getName() != schemaName) 
		      throw new ServerException.Internal(
		            s"Session ID [${sid}]found but in the wrong schema. Expected [${schemaName}], but was [${ssn.schemaGen.getName()}]")

      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      
      Ok(response.toString)
   }

   /**
    * Get a session by ID, if exists in any of the given schema's generations
    * or create in the live gen.
    */
   def getOrCreateSession(schemaName:String, sid:String) = action { req =>
     
      val result: SessionImpl = server.ssnStore.get(sid) match {
         
         // Have the session. Must match the schema name.
         case Some(ssn) => {
            
            if (ssn.schemaGen.getName() != schemaName) 
		      throw new ServerException.Internal(
		            s"Session ID [${sid}]found but in the wrong schema. Expected [${schemaName}], but was [${ssn.schemaGen.getName()}]")
            
            ssn
         }
         
         // Dont have the session. Try to create.
         case None => {
            
            server.schemata.get(schemaName) match {
               
               // Have the schema. Must have a live gen.
               case Some(schema) => {
                  
                  schema.liveGen match {
                     // AOK. Creating the session
                     case Some(liveGen) => new SessionImpl(sid, liveGen)
                     case None => {
                        logger.debug("Schema [%s] not found".format(schemaName))
                        throw new ServerException.Remote(UnknownSchema, schemaName)
                     }
                  }
               }
               
               // Don't have the schema.
               case None => {
                  logger.debug("Schema [%s] not found".format(schemaName))
                  throw new ServerException.Remote(UnknownSchema, schemaName)
               }
            }
         }
      }

      // If we're here, we have the session
      val response = JsObject(Seq(
         "session" -> JsString(result.toJson),
         "schema" -> JsObject(Seq(
               "id" -> JsString(result.schemaGen.getId()),
               "src" -> JsString(result.schemaGen.source)
         ))
      ))

      Ok(response.toString)
   }

   /**
    * Save or replace a new session.
    *
    * IF the session exists THEN
    *   IF the current connection ID matches or is parallel 
    *   to the session's original connection
    *   THEN Replace the session.
    *   ELSE SessionExpired
    * 
    * ELSE (the session does not exist)
    *   IF connection is OPEN, create new session.
    *   ELSE IF connection is DRAINING THEN UnknownConnection
    *   ELSE InternalError
    *   
    **** We should not need a save session.
    *  
   def saveSession() = action { req =>

      val ssnJson = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }

      server.ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
      Ok      
   }
*/
   
   /**
    * 
    */
   def addAttribute() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      val name = (bodyJson \ "name").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "name")         
      }
      val value = (bodyJson \ "value").asOpt[String].getOrElse(null)

      val ssn = server.ssnStore.getOrBust(sid)

      val prevValue = ssn.setAttribute(name, value)
      
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson),
         "returns" -> JsString(prevValue)
      ))
            
      Ok(response.toString())
   }
 
   /**
    * Clear an attribute
    */
   def clearAttribute() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      val name = (bodyJson \ "name").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "name")         
      }

      val ssn = server.ssnStore.getOrBust(sid)
      val prevValue = ssn.clearAttribute(name)
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson),
         "returns" -> JsString(prevValue)
      ))
            
      Ok(response.toString)
   }

}
