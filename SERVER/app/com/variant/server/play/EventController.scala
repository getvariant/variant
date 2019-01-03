package com.variant.server.play

import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.variant.core.impl.ServerError.EmptyBody
import com.variant.core.impl.ServerError.MissingProperty
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.boot.VariantServer
import com.variant.server.event.ServerTraceEvent
import com.variant.server.impl.SessionImpl

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.ControllerComponents

//@Singleton -- Is this for non-shared state controllers?
class EventController @Inject() (
      val action: VariantAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
   
   private val logger = Logger(this.getClass)	
 
   /**
    * POST
    * Trigger a custom event.
    */
   def post() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")            
      }
      
      val eventJson = (bodyJson \ "event").getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "event")            
      }

      val name = (eventJson \ "name").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "name")         
      }
      
      val attrs = (eventJson \ "attrs").asOpt[Map[String,String]].getOrElse {
         Map[String,String]()
      }

      // Get session. Doesn't have to have a request.
      val ssn = server.ssnStore.getOrBust(sid)
                  
      // Trigger the event. 
      ssn.asInstanceOf[SessionImpl].triggerEvent(new ServerTraceEvent(name, attrs.asJava))            
            
      Ok
 
   }  
}

