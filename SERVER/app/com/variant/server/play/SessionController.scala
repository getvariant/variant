package com.variant.server.play

import com.variant.core.impl.ServerError.EmptyBody
import com.variant.core.session.CoreSession
import com.variant.core.impl.ServerError._
import com.variant.core.util.StringUtils
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.api.ServerException
import com.variant.server.boot.SessionStore
import com.variant.server.impl.SessionImpl
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.ControllerComponents
import com.variant.server.boot.VariantServer
import com.variant.server.api.Session
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.schema.SchemaGen
import java.util.Map.Entry
import com.variant.server.boot.ServerExceptionRemote

object SessionController {
   
   val rand = new java.util.Random();
}

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
      if (ssn.schemaGen.getMeta.getName != schemaName) 
		      throw new ServerExceptionRemote(WRONG_CONNECTION, schemaName)

      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson),
         "schema" -> JsObject(Seq(
               "id" -> JsString(ssn.schemaGen.id),
               "src" -> JsString(ssn.schemaGen.source)
         ))      
      ))
      
      Ok(response.toString)
   }

   /**
    * Get a session by ID, if exists in any of the given schema's generations
    * or create in the live gen. This request should always come with the
    * targeting tracker content, but we only use it if we're creating the session.
    */
   def getOrCreateSession(schemaName:String, sid:String) = action { req =>
     
      val bodyJson = req.body.asText.getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }
      
      val trackedExperiences = (Json.parse(bodyJson) \ "tt").asOpt[List[String]].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "tt")
      }
      
      val result: SessionImpl = server.ssnStore.get(sid) match {
         
         // Have the session. Must match the schema name.
         case Some(ssn) => {
            
            if (ssn.schemaGen.getMeta.getName != schemaName) 
		      throw new ServerExceptionRemote(WRONG_CONNECTION, schemaName)
            
            ssn
         }
         
         // Don't have the session. Try to create, but change the SID:
         // we don't want the client think that we found the session.
         case None => {

            val liveGen = server.schemata.getLiveGen(schemaName).getOrElse {
               logger.debug("Schema [%s] not found".format(schemaName))
               throw new ServerExceptionRemote(UNKNOWN_SCHEMA, schemaName)
            }
            
            val newSsn = SessionImpl.empty(StringUtils.random64BitString(SessionController.rand), liveGen)
            val stabile = new SessionScopedTargetingStabile()
            trackedExperiences.foreach { s => stabile.add(SessionScopedTargetingStabile.Entry.parse(s)) }
            newSsn.setTargetingStabile(stabile)
            server.ssnStore.put(newSsn)
            
            
            newSsn
         }
      }

      // If we're here, we have the session
      val response = JsObject(Seq(
         "session" -> JsString(result.toJson),
         "schema" -> JsObject(Seq(
               "id" -> JsString(result.schemaGen.id),
               "src" -> JsString(result.schemaGen.source)
         ))
      ))

      Ok(response.toString)
   }

   /**
    * Save or replace a new session.
    * It's not clear if real code uses it, but the test use it to create a particular session state.
    *
    * IF the session exists THEN
    *   IF its gen must matches the schema in request, 
    *   THEN Replace the session.
    *   ELSE SessionExpired  // Really weird case !!!
    * 
    * ELSE (the session does not exist)
    *   IF given schema exists and has a live gen, create
    *   ELSE throw UnknownSchema error.
    *   
    */  
   def saveSession(schemaName: String) = action { req =>
      
      val ssnJson = req.body.asText.getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }
      
      val sid = (Json.parse(ssnJson) \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")
      }
      
      val gen = server.ssnStore.get(sid) match {
         
         case Some(ssn) => 
            
            // Must match the schema name.
            if (ssn.schemaGen.getMeta.getName != schemaName) 
		      throw new ServerExceptionRemote(WRONG_CONNECTION, schemaName)

            ssn.schemaGen
            
         case None =>
            // Given schema must have a live gen.
            server.schemata.getLiveGen(schemaName).getOrElse {
               throw new ServerExceptionRemote(UNKNOWN_SCHEMA, schemaName)
            }
      }
      
      val ssn = SessionImpl(ssnJson, gen)
      server.ssnStore.put(ssn)
      
      Ok      
   }

   
   /**
    * 
    */
   def addAttribute() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")         
      }
      val name = (bodyJson \ "name").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "name")         
      }
      val value = (bodyJson \ "value").asOpt[String].getOrElse(null)

      val ssn = server.ssnStore.getOrBust(sid)

      // Serialize the session before adding the attribute.
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))

      ssn.getAttributes.put(name, value)
      
      Ok(response.toString())
   }
 
   /**
    * Clear an attribute
    */
   def clearAttribute() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")         
      }
      val name = (bodyJson \ "name").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "name")         
      }

      val ssn = server.ssnStore.getOrBust(sid)
      
      // Serialize the session before removing the attribute.
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))

      ssn.getAttributes.remove(name)
            
      Ok(response.toString)
   }

}
