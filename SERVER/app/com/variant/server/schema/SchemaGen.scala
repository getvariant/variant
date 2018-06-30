package com.variant.server.schema

import java.util.concurrent.atomic.AtomicInteger

import com.variant.core.schema.{ Schema => CoreSchema }
import com.variant.core.schema.parser.ParserResponse
import com.variant.core.util.StringUtils
import com.variant.server.boot.Runtime
import com.variant.server.event.EventWriter

import play.api.Logger

/**
 * 
 */
object SchemaGen {
   
   private val rand = new java.util.Random()
   
   def apply(response: ParserResponse, origin: String) = new SchemaGen(response, origin)
}

/**
 * Server side schema adds some server specific state.
 */
class SchemaGen(val response: ParserResponse, val origin: String) extends CoreSchema {
  
   import State._
   
   private val logger = Logger(this.getClass)   
   private val coreSchema =  response.getSchema
   private val id = StringUtils.random64BitString(SchemaGen.rand)
   
   /**
    * Schema can be New, Deployed or Gone
    */
   var state: State = New  
   
   /**
    * Number of sessions connected to this schema generation.
    */
   val sessionCount = new AtomicInteger(0)

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
      state = Dead
	   logger.info(s"Undeployed schema generation [${getName}] ID [${getId}], from [${origin}]")
	}
   
   /**
    * 
    */
   override def toString() = s"{ServerSchema=[$getName], ID=[$getId]}"
   
}

/**
 * Lifecycle states of a schema generation.
 */
object State extends Enumeration {
   type State = Value
   val New, Live, Dead = Value
}
