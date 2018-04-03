package com.variant.server.controller

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

//@Singleton -- Is this for non-shared state controllers?
class EventController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore) 
      extends VariantController  {
   
   private val logger = Logger(this.getClass)	
 
   /**
    * Trigger a remote event, i.e. write it off to external storage.
    */
   def post() = VariantAction { req =>

      val bodyJson = req.body.asJson.getOrElse {
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

      val ssn = ssnStore.getOrBust(sid, getCIDOrBust(req))
      
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
      Ok
         
   }  
}

