package com.variant.server.schema

import com.variant.core.schema.parser.SchemaParser
import com.variant.core.impl.UserHooker
import play.api.Application

/**
 * Server side schema parser uses real hooker.
 */
object ServerSchemaParser {   
   def apply() = new ServerSchemaParser()
}

class ServerSchemaParser () extends SchemaParser {
   
   private[schema] val hooker = new ServerHooker()
   
   override def getHooker(): UserHooker = hooker
   
}