package com.variant.server.controller

import scala.collection.JavaConversions._
import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import play.api.libs.json._
import com.variant.core.ServerError._
import java.util.Date
import play.api.mvc.Result
import play.api.mvc.AnyContent
import play.api.libs.json.JsValue
import play.api.http.HeaderNames
import scala.collection.mutable.Map
import com.variant.server.event.ServerEvent
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.conn.ConnectionStore
import com.variant.server.boot.VariantServer
import com.variant.server.api.ServerException
import com.variant.server.conn.Connection
import com.variant.server.api.Session
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._
import com.variant.core.ConnectionStatus._
import play.api.mvc.ControllerComponents

//@Singleton -- Is this for non-shared state controllers?
class EventController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore,
      val variantAction: VariantAction,
      val cc: ControllerComponents
      ) extends VariantController(variantAction, cc)  {
   
   private val logger = Logger(this.getClass)	
 
   /**
    * POST
    * Trigger an event.
    */
   def post() = variantAction { req =>

      val conn = connStore.getOrBust(getConnIdOrBust(req))
      
      try {         
         val bodyJson = getBody(req).getOrElse {
            throw new ServerException.Remote(EmptyBody)
         }
         
         val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
            throw new ServerException.Remote(MissingProperty, "sid")            
         }
         
         val name = (bodyJson \ "name").asOpt[String].getOrElse {
            throw new ServerException.Remote(MissingProperty, "name")         
         }
         
         val value = (bodyJson \ "value").asOpt[String].getOrElse {
            throw new ServerException.Remote(MissingProperty, "value")         
         }
         
         val timestamp = (bodyJson \ "ts").asOpt[Long].getOrElse(System.currentTimeMillis())
         
         val params = (bodyJson \ "params").asOpt[List[JsObject]].getOrElse(List[JsObject]())
   
         val ssn = ssnStore.getOrBust(sid, conn.id)
         
         if (ssn.getStateRequest == null)
            throw new ServerException.Remote(UnknownState)   
   
         val event = new ServerEvent(name, value, new Date(timestamp));  
         
         params.foreach(p => {
            val name = (p \ "name").asOpt[String].getOrElse {
               throw new ServerException.Remote(MissingParamName)
            }
            val value = (p \ "value").asOpt[String].getOrElse("")
            event.setParameter(name, value)
         })
         
         ssn.asInstanceOf[SessionImpl].triggerEvent(event)            
      }
      // If we're going out with an exception, don't forget to attach the connection status header.
      catch {
         case rex: ServerException.Remote => 
            throw rex.withHeaders(Map(HTTP_HEADER_CONN_STATUS -> conn.status.toString))
      }
            
      Ok.withHeaders(HTTP_HEADER_CONN_STATUS -> conn.status.toString())
         
   }  
}

