package com.variant.server.schema

import scala.collection.JavaConversions._
import com.variant.core.schema.Schema
import java.io.File
import com.variant.core.schema.parser.ParserResponse
import com.variant.server.api.ServerException
import com.variant.core.impl.UserError.Severity
import com.variant.core.schema.Hook
import com.variant.server.boot.VariantServer
import com.variant.core.schema.parser.HooksService
import com.variant.server.api.EventFlusher
import com.variant.core.schema.parser.FlusherService
import play.api.Logger
import com.variant.server.boot.Runtime
import com.variant.server.event.EventWriter
import java.util.concurrent.atomic.AtomicInteger
import com.variant.core.util.StringUtils

/**
 * 
 */
object ServerSchema {
   def apply(response: ParserResponse, origin: String) = new ServerSchema(response, origin)
}

/**
 * Server side schema adds some server specific semantics.
 */
class ServerSchema (val response: ParserResponse, val origin: String) extends Schema {
  
   import State._
   
   private val logger = Logger(this.getClass)   
   private val rand = new java.util.Random()
   private val coreSchema =  response.getSchema
   private val id = StringUtils.random64BitString(rand)
   
   /**
    * Schema can be New, Deployed or Gone
    */
   var state: State = New  
   
   /**
    * Number of sessions connected to this schema
    * over any parallel connection.
    */
   val sessionCount = new AtomicInteger(0)

   /**
    * 
    *
   private def checkState {
      if (state != Deployed)
         throw new ServerException.Internal(
               "Schema [%s] cannot be accessed due to state [%s]".format(getName, state))
   }
   */
   /*------------------------------------ Public Implementations ------------------------------------*/

   override def getName = {
	   coreSchema.getName
	}

   override def getComment = {
	   coreSchema.getComment
	}

   override def getHooks = {
	   coreSchema.getHooks
	}

   override def getFlusher = {
	   coreSchema.getFlusher
	}

   override def getId = id

	override def getStates = {
	   coreSchema.getStates
	}

	override def getState(name: String) = {
	   coreSchema.getState(name)	   
	}

	override def getTests() = {
	   coreSchema.getTests
	}
	
	override def getTest(name: String) = {
	   coreSchema.getTest(name)	   
	}

	/*------------------------------------ Public Extensions ------------------------------------*/
	
   val runtime = new Runtime(this)
	val source = response.getSchemaSrc
	val hooksService = response.getParser.getHooksService.asInstanceOf[ServerHooksService]
   val flusherService = response.getParser.getFlusherService.asInstanceOf[ServerFlusherService]
	val eventWriter = new EventWriter(flusherService)
	
   /**
    * Undeploy this schema.
    */
	def undeploy() {
      state = Gone
      VariantServer.instance.connStore.drainConnectionsToSchema(getId)
	   logger.info("Undeployed schema [%s] ID [%s], from [%s]".format(getName, getId, origin))
	}
   
   /**
    * 
    */
   override def toString() = s"{ServerSchema=[$getName], ID=[$getId]}"
   
}

/**
 * Schema lifecycle states.
 */
object State extends Enumeration {
   type State = Value
   val New, Deployed, Gone = Value
}
