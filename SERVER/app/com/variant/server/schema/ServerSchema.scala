package com.variant.server.schema

import scala.collection.JavaConversions._
import com.variant.core.schema.Schema
import java.io.File
import com.variant.core.schema.parser.ParserResponse
import com.variant.server.api.ServerException
import com.variant.core.UserError.Severity
import com.variant.core.schema.Hook
import com.variant.server.boot.VariantServer
import com.variant.core.schema.parser.HooksService
import com.variant.server.api.EventFlusher
import com.variant.core.schema.parser.FlusherService
import play.api.Logger
import com.variant.server.boot.Runtime
import com.variant.server.event.EventWriter

/**
 * 
 */
object ServerSchema {
   def apply(response: ParserResponse, hookService: ServerHooksService, flushService: ServerFlusherService) = 
     new ServerSchema(response, hookService, flushService)
}

/**
 * Server side schema adds some server specific semantics.
 */
class ServerSchema (val response: ParserResponse, val hookService: ServerHooksService, val flushService: ServerFlusherService) extends Schema {
  
   import State._
   
   private val logger = Logger(this.getClass)   
   
   private var coreSchema =  response.getSchema

   var state: State = Deployed  
      
   /**
    * 
    */
   private def checkState {
      if (state != Deployed)
         throw new ServerException.Internal(
               "Schema [%s] cannot be accessed due to state [%s]".format(getName, state))
   }

   /*------------------------------------ Public Implementations ------------------------------------*/

   override def getName = {
	   checkState
	   coreSchema.getName
	}

   override def getComment = {
	   checkState
	   coreSchema.getComment
	}

   override def getHooks = {
	   checkState
	   coreSchema.getHooks
	}

   override def getFlusher = {
	   checkState
	   coreSchema.getFlusher
	}

   override def getId = {
	   checkState
	   coreSchema.getId
	}

	override def getStates = {
	   checkState
	   coreSchema.getStates
	}

	override def getState(name: String) = {
	   checkState
	   coreSchema.getState(name)	   
	}

	override def getTests() = {
      checkState
	   coreSchema.getTests
	}
	
	override def getTest(name: String) = {
      checkState
	   coreSchema.getTest(name)	   
	}

	/*------------------------------------ Public Extensions ------------------------------------*/
	
  val runtime = new Runtime(this)
	val source = response.getSchemaSrc
	val eventWriter = new EventWriter(flushService)
	
	def undeploy(): Unit = {
	   logger.info("Schema [%s], ID [%s] undeployed".format(getName, getId))
	}
}

/**
 * Schema lifecycle states.
 */
object State extends Enumeration {
   type State = Value
   val Deployed, Undeployed, Gone = Value
}
