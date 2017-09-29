package com.variant.server.schema

import scala.collection.mutable
import scala.collection.immutable
import scala.collection.JavaConversions._
import play.api.Logger
import com.variant.core.schema.parser.ParserResponse
import com.variant.core.schema.parser.FlusherService
import com.variant.core.schema.parser.HooksService
import com.variant.core.UserError.Severity
import com.variant.core.schema.ParserMessage
import com.variant.server.boot.ServerErrorLocal

abstract class AbstractSchemaDeployer() extends SchemaDeployer {

  private val logger = Logger(this.getClass)
  
  private var hooker: ServerHooksService = _
  private var flusher: ServerFlusherService = _

  private var _parserResponses = mutable.Seq[ParserResponse]()
  override lazy val parserResponses = _parserResponses.toSeq
  
  /**
   * Parse a schema.
   */
  protected def parse(schemaSrc: String): ParserResponse = {

    val parser = new ServerSchemaParser()
    hooker = parser.getHooksService.asInstanceOf[ServerHooksService]
    flusher = parser.getFlusherService.asInstanceOf[ServerFlusherService]
    
    // Parser the schema.
    val resp = parser.parse(schemaSrc)

    // Only check for duplicate schema name if no parse errors.
    if (!resp.hasMessages(Severity.ERROR) &&  schemata.contains(resp.getSchema.getName))
         resp.addMessage(ServerErrorLocal.SCHEMA_NAME_DUPE, resp.getSchema.getName)

    // Log all parser messages.
    resp.getMessages(Severity.ERROR).foreach { logParserMessage(_) }
    
    _parserResponses = _parserResponses :+ resp
    resp
  }
 
  /**
   * Deploy a parsed schema.
   */
  protected def deploy(parserResp: ParserResponse): ServerSchema = {

    val schema = ServerSchema(parserResp, hooker, flusher)

    // Write log message
    val msg = new StringBuilder()
    msg.append("Deployed schema [%s], ID [%s]:".format(schema.getName, schema.getId));
    for (test <- schema.getTests) {
      msg.append("\n   ").append(test.getName()).append(":[");
      var first = true;
      for (exp <- test.getExperiences()) {
        if (first) first = false;
        else msg.append(", ");
        msg.append(exp.getName);
        if (exp.isControl) msg.append(" (control)");
      }
      msg.append("]");
      msg.append(" (").append(if (test.isOn()) "ON" else "OFF").append(")");
    }

    logger.info(msg.toString())
  
    schema
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
