package com.variant.server.play

import scala.collection.mutable
import scala.collection.JavaConversions._
import javax.inject.Inject
import play.api.mvc.Request
import com.variant.server.boot.SessionStore
import play.api.Logger
import play.api.libs.json._
import com.variant.core.impl.ServerError._
import java.util.Date
import play.api.mvc.Result
import play.api.mvc.AnyContent
import play.api.libs.json.JsValue
import play.api.http.HeaderNames
import com.variant.server.event.ServerTraceEvent
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.boot.VariantServer
import com.variant.server.api.ServerException
import com.variant.server.api.Session
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._
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
         throw new ServerException.Remote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")            
      }
      
      val eventJson = (bodyJson \ "event").getOrElse {
         throw new ServerException.Remote(MissingProperty, "event")            
      }

      val name = (eventJson \ "name").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "name")         
      }
      
      val value = (eventJson \ "value").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "value")         
      }

      val attrs = (eventJson \ "attrList").asOpt[Seq[Map[String,String]]].getOrElse {
         Seq[Map[String,String]]()
      }

      // Convert attrs from a list of single-element maps to one map
      val attrMap = mutable.Map[String,String]()
      attrs.foreach { _.foreach { case (k,v) => attrMap(k) = v }}

      val ssn = server.ssnStore.getOrBust(sid)
      
      if (ssn.getStateRequest == null)
         throw new ServerException.Remote(UNKNOWN_STATE)   

      val event = new ServerTraceEvent(name, value, attrMap);  
            
      ssn.asInstanceOf[SessionImpl].triggerEvent(event)            
            
      Ok
 
   }  
}

