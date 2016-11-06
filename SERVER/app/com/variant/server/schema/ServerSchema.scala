package com.variant.server.schema

import com.variant.core.schema.Schema
import scala.collection.JavaConverters._

/**
 * Server side schema adds some server specific semantics.
 */
class ServerSchema (coreSchema: Schema) extends Schema {
  
   private def isValid = {
      true
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

}