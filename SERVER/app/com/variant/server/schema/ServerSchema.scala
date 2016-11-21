package com.variant.server.schema

import com.variant.core.schema.Schema
import scala.collection.JavaConverters._
import java.io.File
import com.variant.core.schema.ParserResponse

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
   
   private var state: State = New
   
   private def isValid = {
      state == Deployed
   }

   override def getId = {
	   isValid
	   coreSchema.getId
	}

	override def getStates = {
	   isValid
	   coreSchema.getStates
	}

	override def getState(name: String) = {
	   isValid
	   coreSchema.getState(name)	   
	}

	override def getTests() = {
      isValid
	   coreSchema.getTests
	}
	
	override def getTest(name: String) = {
      isValid
	   coreSchema.getTest(name)	   
	}

	def setState(state: State) {
	   this.state = state
	}
	
}

/**
 * Schema lifecycle states.
 */
object State extends Enumeration {
   type State = Value
   val New, Failed, Deployed, Undeployed = Value
}
