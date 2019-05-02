package com.variant.server.play

import javax.inject.Inject
import play.api.Logger
import com.variant.core.error.ServerError._
import com.variant.server.boot.ServerErrorRemote
import play.api.mvc.ControllerComponents
import com.variant.server.boot.VariantServer
import play.api.libs.json._
import com.variant.server.api.ConfigKeys
import scala.io.Source

class ConnectionController @Inject() (
      val action: VariantAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
   
   private val logger = Logger(this.getClass)	

   /**
    * Ping a schema.
    * Called on VariantClient.connectTo(schema)
    */
   def get(name: String) = action { req =>
            
      server.schemata.get(name) match {
        
         case Some(schema) => {
            logger.debug("Schema [%s] found".format(name))
            
            val response = JsObject(Seq(
               "ssnto" -> JsNumber(VariantServer.instance.config.getSessionTimeout)
            ))
            
            Ok(response.toString())
         }
         case None => {
            logger.debug("Schema [%s] not found".format(name))
            ServerErrorRemote(UNKNOWN_SCHEMA).asResult(name)
         }
      }
   } 
   
}
