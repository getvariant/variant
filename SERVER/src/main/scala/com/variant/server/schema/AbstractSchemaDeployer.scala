package com.variant.server.schema

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable

import com.variant.core.error.UserError.Severity
import com.variant.core.schema.parser.ParserMessage
import com.variant.core.schema.parser.ParserResponse
import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.VariantServer

abstract class AbstractSchemaDeployer(implicit server: VariantServer) extends SchemaDeployer with LazyLogging {

   private[this] val _parserResponses = mutable.ArrayBuffer[ParserResponse]()

   override lazy val parserResponses = _parserResponses.toSeq

   /**
    * Parse a schema.
    */
   protected def parse(schemaSrc: String): ParserResponse = {

      val parser = ServerSchemaParser(implicitly)

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
      schemata.deploy(SchemaGen(parserResp, origin))
   }

   /**
    * Append parser message to the system logger with the appropriate severity.
    */
   private def logParserMessage(msg: ParserMessage): Unit = {
      msg.getSeverity match {
         case Severity.FATAL | Severity.ERROR =>
            if (msg.getException == null) logger.error(msg.toString)
            else logger.error(msg.toString(), msg.getException)
         case Severity.WARN => logger.warn(msg.toString)
         case Severity.INFO => logger.info(msg.toString)
      }
   }
}
