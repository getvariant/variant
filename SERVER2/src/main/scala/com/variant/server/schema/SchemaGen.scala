package com.variant.server.schema

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._
import com.variant.core.schema.{ Schema => CoreSchema }
import com.variant.core.schema.parser.ParserResponse
import com.variant.core.util.StringUtils
import com.variant.server.boot.Runtime
import com.variant.server.impl.TraceEventWriter

/**
 * 
 */
object SchemaGen {
   
   /**
	 * Lifecycle states of a schema generation.
	 */
   object State extends Enumeration {
      type State = Value
      val New, Live, Dead = Value
   }

   private val rand = new java.util.Random()
   
   def apply(response: ParserResponse, origin: String) = new SchemaGen(response, origin)
}

/**
 * Server side schema adds some server specific state.
 */
class SchemaGen(val response: ParserResponse, val origin: String) extends CoreSchema {
           
   //private val logger = Logger(this.getClass)   
   private val coreSchema =  response.getSchema

   val id = StringUtils.random64BitString(SchemaGen.rand)
  
   // Public access for tests only!
   var state = SchemaGen.State.New  
      
   /**
    * Number of sessions connected to this schema generation.
    */
   val sessionCount = new AtomicInteger(0)

   /*------------------------------------ Public Implementations ------------------------------------*/

   override def getMeta = {
	   coreSchema.getMeta
	}


	override def getStates = {
	   coreSchema.getStates
	}

	override def getState(name: String) = {
	   coreSchema.getState(name)	   
	}

	override def getVariations() = {
	   coreSchema.getVariations
	}
	
	override def getVariation(name: String) = {
	   coreSchema.getVariation(name)	   
	}

	/*------------------------------------ Public Extensions ------------------------------------*/
	
   val runtime = new Runtime(this)
   val source = response.getSchemaSrc
	 val hooksService = response.getParser.getHooksService.asInstanceOf[ServerHooksService]
   val flusherService = response.getParser.getFlusherService.asInstanceOf[ServerFlusherService]
	 val eventWriter = new TraceEventWriter(flusherService)
	     
   /**
    * 
    */
   override def toString() = s"{ServerSchema=[${getMeta.getName}], ID=[${id}]}"
   
}
