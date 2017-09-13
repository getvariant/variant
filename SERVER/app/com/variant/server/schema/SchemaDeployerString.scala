package com.variant.server.schema

import scala.collection.JavaConversions._
import play.api.Logger
import com.variant.core.UserError.Severity
import com.variant.core.schema.ParserMessage

/**
 * Deploy single schema from a string in memory.
 */
class SchemaDeployerString(schemaSrc: String) extends AbstractSchemaDeployer {

  private val logger = Logger(this.getClass)

  override val schemata = {
    
    // Parse
    val parserResp = parse(schemaSrc)
   
    // If failed parsing, print errors and no schema.
    if (parserResp.hasMessages(Severity.ERROR)) {
      logger.error("Schema was not deployed due to previous parser error(s)")
      Seq()
    }
    else {
      Seq(deploy(parserResp))
    }   
  }
}
