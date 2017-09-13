package com.variant.server.schema

import scala.collection.JavaConversions._
import play.api.Logger
import com.variant.core.schema.parser.ParserResponse
import com.variant.core.schema.parser.FlusherService
import com.variant.core.schema.parser.HooksService
import com.variant.core.UserError.Severity
import com.variant.core.schema.ParserMessage

abstract class AbstractSchemaDeployer() extends SchemaDeployer {

  private val logger = Logger(this.getClass)
  
  private var hooker: ServerHooksService = _
  private var flusher: ServerFlusherService = _

  /**
   * Parse a schema.
   */
  protected def parse(schemaSrc: String): ParserResponse = {

    hooker = new ServerHooksService()
    flusher = new ServerFlusherService()

    val resp = ServerSchemaParser(hooker, flusher).parse(schemaSrc)

    // Log all parser messages.
    resp.getMessages(Severity.ERROR).foreach { logParserMessage(_) }
    
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
