package com.variant.server.schema

import play.api.Logger
import scala.collection.mutable
import com.variant.server.api.ServerException


trait SchemaDeployer {

  /**
   * Once constructed, a schema deployer returns all available schemata.
   * The returned collection may change over time, as some deployers may
   * scan periodically for updates.
   */
  def schemata: Map[String, ServerSchema]
  
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
  def fromString(schemaSrc: String): SchemaDeployer = new SchemaDeployerString(schemaSrc)

  /**
   * Factory method returns a deployer that reads schema from a classpath resource once.
   */
  def fromClasspath(resource: String): SchemaDeployer = new SchemaDeployerClasspath(resource)

}


