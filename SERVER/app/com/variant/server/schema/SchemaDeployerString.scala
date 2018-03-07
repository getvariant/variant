package com.variant.server.schema

import scala.collection.JavaConversions._
import play.api.Logger
import com.variant.core.UserError.Severity
import com.variant.core.schema.ParserMessage
import com.variant.core.util.IoUtils
import scala.collection.mutable.HashMap

/**
 * Deploy single schema from a string in memory.
 */
class SchemaDeployerString(schemaStrings: String*) extends AbstractSchemaDeployer {

   private val logger = Logger(this.getClass)

   private val _schemata = HashMap[String, ServerSchema]()
  
   // Convert internal mutable map to an immutable one for the world
   override def schemata = _schemata.toMap  
  
   schemaStrings.foreach { (schemaSrc:String) =>
      
      val parserResponse = parse(schemaSrc)
       
      // If failed parsing, print errors and no schema.
      if (parserResponse.hasMessages(Severity.ERROR)) {
         logger.error("Schema was not deployed due to previous parser error(s)")
      }
      else {
         val schema = deploy(parserResponse)
         _schemata += schema.getName -> schema
      }   
      
   }

}

/**
 * Deploy single schema from classpath.
 */
class SchemaDeployerClasspath(resource: String)
   extends SchemaDeployerString(IoUtils.toString(IoUtils.openFileAsStream(resource)))
