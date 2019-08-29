package com.variant.server.schema

import com.variant.core.schema.parser.SchemaParser
import com.variant.core.schema.parser.HooksService
import com.variant.core.schema.parser.FlusherService
import com.variant.server.boot.VariantServer

/**
 * Server side schema parser uses real hooker.
 */
object ServerSchemaParser {

   def apply(implicit server: VariantServer) = new ServerSchemaParser()

}

/**
 * Server side implementation of schema parser, complete with server side services.
 */
class ServerSchemaParser(implicit server: VariantServer) extends SchemaParser {

   override val getHooksService: HooksService = new ServerHooksService()
   override val getFlusherService: FlusherService = new ServerFlusherService(server.config, this)

}

