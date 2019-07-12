package com.variant.server.schema

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable

import com.variant.core.error.UserError.Severity
import com.variant.core.schema.parser.ParserMessage
import com.variant.core.schema.parser.ParserResponse
import com.typesafe.scalalogging.LazyLogging

abstract class AbstractSchemaDeployer() extends SchemaDeployer with LazyLogging {
  
  private[this] val _parserResponses = mutable.ArrayBuffer[ParserResponse]()
  
  override lazy val parserResponses = _parserResponses.toSeq
  
  override val schemata = new Schemata
  
  /**
   * Parse a schema.
   */
  protected def parse(schemaSrc: String): ParserResponse = {

    val parser = ServerSchemaParser()
    
    // Parser the schema.
    val resp = parser.parse(schemaSrc)

    // Log all parser messages.
    resp.getMessages(Severity.ERROR).asScala.foreach { logParserMessage(_) }
    
    _parserResponses += resp
    resp
  }
 
  /**
   * Deploy a parsed schema.
   */
  protected def deploy(parserResp: ParserResponse, origin: String) {
     schemata.deploy(SchemaGen(parserResp, origin) )
  }

  /**
   * Append parser message to the system logger with the appropriate severity.
   */
  private def logParserMessage(msg: ParserMessage): Unit = {
    msg.getSeverity match {
        case Severity.FATAL | Severity.ERROR => logger.error(msg.toString())
        case Severity.WARN => logger.warn(msg.toString())
        case Severity.INFO => logger.info(msg.toString())
    }     
  }
}
