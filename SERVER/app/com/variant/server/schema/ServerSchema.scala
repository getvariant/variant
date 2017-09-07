package com.variant.server.schema

import scala.collection.JavaConversions._
import com.variant.core.schema.Schema
import java.io.File
import com.variant.core.schema.ParserResponse
import com.variant.server.api.ServerException
import com.variant.core.UserError.Severity
import com.variant.core.schema.Hook
import com.variant.server.boot.VariantServer
import com.variant.core.schema.parser.HooksService

/**
 * 
 */
object ServerSchema {
   def apply(response: ParserResponse, hooker: HooksService) = new ServerSchema(response, hooker)
}

/**
 * Server side schema adds some server specific semantics.
 */
class ServerSchema (val response: ParserResponse, val hooker: HooksService) extends Schema {
  
   import State._
   
   var state: State = New
   
   private var coreSchema =  response.getSchema
   
   if (response.hasMessages(Severity.ERROR)) {
      // Schema did not actually parse.
	   state = State.Failed
   }
      
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
	
	val source = response.getSchemaSrc
}

/**
 * Schema lifecycle states.
 */
object State extends Enumeration {
   type State = Value
   val New, Failed, Deployed, Undeployed = Value
}
