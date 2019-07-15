package com.variant.server.schema

import scala.collection.mutable
import com.variant.core.schema.parser.ParserResponse

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

object SchemaDeployer {

   /**
    * Factory method returns a file system deployer.
    */
   def fromFileSystem(): SchemaDeployer = new SchemaDeployerFileSystem()

   /**
    * Factory method returns a deployer that reads schema from a memory string once.
    */
   def fromString(schemaSrc: String*): SchemaDeployer = new SchemaDeployerString(schemaSrc: _*)

   /**
    * Factory method returns a deployer that reads schema from a classpath resource once.
    */
   def fromClasspath(resource: String): SchemaDeployer = new SchemaDeployerClasspath(resource)

}

