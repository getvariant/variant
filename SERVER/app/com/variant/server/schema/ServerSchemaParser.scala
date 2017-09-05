package com.variant.server.schema

import com.variant.core.schema.parser.SchemaParser
import com.variant.core.schema.parser.HooksService
import com.variant.core.schema.parser.FlusherService

/**
 * Server side schema parser uses real hooker.
 */
object ServerSchemaParser {   
   def apply() = new ServerSchemaParser()
}

/**
 * Server side implementation of schema parser, complete with server side services.
 */
class ServerSchemaParser () extends SchemaParser {
   
   private[this] val hooksService = new ServerHooksService()
   private[this] val fluhserService = new ServerFlusherService()
   
   override def getHooksService(): HooksService = hooksService
   override def getFlusherService(): FlusherService = fluhserService
   
}