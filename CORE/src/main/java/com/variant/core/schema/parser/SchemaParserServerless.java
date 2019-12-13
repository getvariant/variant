package com.variant.core.schema.parser;

/**
 * Serverless schema parser can be used to parse the schema with null services.
 */
public class SchemaParserServerless extends SchemaParser {

   @Override
   public HooksService getHooksService() {
      return new HooksService.Null();
   }

   @Override
   public FlusherService getFlusherService() {
      return new FlusherService.Null();
   }

}
