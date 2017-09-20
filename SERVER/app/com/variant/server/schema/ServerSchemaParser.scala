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
      
   override val getHooksService: HooksService = new ServerHooksService()
   override val getFlusherService: FlusherService = new ServerFlusherService(this)

}

