package com.variant.server.schema

import com.variant.server.api.ServerException
import org.apache.commons.io.IOUtils

/**
 * 
 */
object SchemaDeployerClasspath {
  
  /**
   * Read resource into a memory string.
   */
  private def read(resource:String):String = {
    val stream = getClass.getResourceAsStream(resource)
    if (stream == null)
      throw new ServerException.Internal("Unable to open classpath resource [%s]".format(resource))
    IOUtils.toString(stream)
  }
}

/**
 * Deploy single schema from classpath.
 */
class SchemaDeployerClasspath(resource: String) extends SchemaDeployerString(SchemaDeployerClasspath.read(resource)) {}
