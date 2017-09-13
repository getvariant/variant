package com.variant.server.schema

import com.variant.core.schema.parser.SchemaParser
import com.variant.core.schema.parser.HooksService
import com.variant.core.schema.parser.FlusherService

/**
 * Server side schema parser uses real hooker.
 */
object ServerSchemaParser {   
   def apply(hooker: HooksService, flusher: FlusherService) = new ServerSchemaParser(hooker, flusher)
}

/**
 * Server side implementation of schema parser, complete with server side services.
 */
class ServerSchemaParser (private val hooker: HooksService, private val flusher: FlusherService) extends SchemaParser {
      
   override def getHooksService(): HooksService = hooker
   override def getFlusherService(): FlusherService = flusher

}

