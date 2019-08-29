package com.variant.server.schema

import scala.collection.mutable
import com.variant.core.schema.parser.ParserResponse
import com.variant.server.boot.VariantServer

trait SchemaDeployer {

   /**
    * Once constructed, a schema deployer returns all available schemata at startup time.
    */
   val schemata = new Schemata

   /**
    * All parser response objects in order they were produced by the deployer,
    * regardless whether the schema was deployed or not.
    */
   def parserResponses: Seq[ParserResponse]

   /**
    * Subclasses should override this, if there's anything to be done prior to parsing.
    */
   def bootstrap() = {}

}

/**
 * Note that non-file system deployers no longer work. Works is needed to make them work again
 * if we need them. For now, if you need a custom schema, use mix-in the TempSchemaDir trait
 * and create your custom schemata there.
 */
object SchemaDeployer {

   /**
    * Factory method returns a file system deployer.
    */
   def fromFileSystem(implicit server: VariantServer): SchemaDeployer = new SchemaDeployerFileSystem()

   /**
    * Factory method returns a deployer that reads schema from a memory string once.
    */
   //def fromString(schemaSrc: String*)(implicit server: VariantServer): SchemaDeployer = new SchemaDeployerString(schemaSrc: _*)

   /**
    * Factory method returns a deployer that reads schema from a classpath resource once.
    */
   //def fromClasspath(resource: String)(implicit server: VariantServer): SchemaDeployer = new SchemaDeployerClasspath(resource)

}

