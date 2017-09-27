package com.variant.server.schema

import scala.collection.JavaConversions._
import play.api.Logger
import com.variant.core.UserError.Severity
import com.variant.core.schema.ParserMessage

object SchemaDeployerString {

  def apply(schemaStr: String) = new SchemaDeployerString(schemaStr)
  
}

/**
 * Deploy single schema from a string in memory.
 */
class SchemaDeployerString(schemaStr: String) extends AbstractSchemaDeployer {

  private val logger = Logger(this.getClass)

  val parserResponse = parse(schemaStr)
  
  override val schemata: Map[String, ServerSchema] = {
       
    // If failed parsing, print errors and no schema.
    if (parserResponse.hasMessages(Severity.ERROR)) {
      logger.error("Schema was not deployed due to previous parser error(s)")
      Map()
    }
    else {
       val schema = deploy(parserResponse)
       Map(schema.getName -> schema)
    }   
  }
}
