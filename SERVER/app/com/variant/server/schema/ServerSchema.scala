package com.variant.server.schema

import com.variant.core.schema.Schema
import scala.collection.JavaConverters._
import java.io.File
import com.variant.core.schema.ParserResponse
import com.variant.core.exception.RuntimeInternalException

/**
 * 
 */
object ServerSchema {
   def apply(name: String, schema: Schema) = new ServerSchema(name, schema)
}

/**
 * Server side schema adds some server specific semantics.
 */
class ServerSchema (val name: String, val coreSchema: Schema) extends Schema {
  
   import State._
   
   var state: State = New
   
   private def checkState {
      if (state != Deployed)
         throw new RuntimeInternalException(
               "Schema [%s] cannot be accessed due to state [%s]".format(name, state))
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
	
}

/**
 * Schema lifecycle states.
 */
object State extends Enumeration {
   type State = Value
   val New, Failed, Deployed, Undeployed = Value
}
