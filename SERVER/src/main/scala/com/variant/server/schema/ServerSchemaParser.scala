package com.variant.server.schema

import com.variant.share.schema.parser.SchemaParser
import com.variant.share.schema.parser.HooksService
import com.variant.share.schema.parser.FlusherService
import com.variant.server.boot.VariantServer

/**
 * Server side schema parser uses real hooker.
 */
object ServerSchemaParser {

   /**
    * Regular invocation
    */
   def apply(implicit server: VariantServer) = new ServerSchemaParser(Some(server))

   /**
    * Tests parse with null services, without initializing hooks and flushers.
    */
   def apply() = new ServerSchemaParser(None)

}

/**
 * Server side implementation of schema parser, complete with server side services.
 */
class ServerSchemaParser private (server: Option[VariantServer]) extends SchemaParser {

   override val getHooksService: HooksService = server match {
      case Some(_) => new ServerHooksService()
      case None => new HooksService.Null()
   }
   
   override val getFlusherService: FlusherService = server match {
      case Some(svr) => new ServerFlusherService(svr.config, this)
      case None =>  new FlusherService.Null()
      
   }

}

