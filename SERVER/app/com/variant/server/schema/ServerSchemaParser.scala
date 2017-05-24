package com.variant.server.schema

import com.variant.core.schema.parser.SchemaParser
import com.variant.core.impl.UserHooker

/**
 * Server side schema parser uses real hooker.
 */
object ServerSchemaParser {   
   def apply(hooker: UserHooker) = new ServerSchemaParser(hooker)
}

class ServerSchemaParser(val hooker: UserHooker) extends SchemaParser {
   override def getHooker(): UserHooker = hooker
}