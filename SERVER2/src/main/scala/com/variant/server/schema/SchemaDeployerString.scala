package com.variant.server.schema

import java.util.Random

import com.variant.core.error.UserError.Severity
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils

/**
 * Deploy single schema from a string in memory.
 */
class SchemaDeployerString(schemaStrings: String*) extends AbstractSchemaDeployer {

   // Convert internal mutable map to an immutable one for the world
   //override def schemata = _schemata.toMap

   schemaStrings.foreach { schemaSrc =>

      val parserResponse = parse(schemaSrc)

      // If failed parsing, print errors and no schema.
      if (parserResponse.hasMessages(Severity.ERROR)) {
         logger.error("Schema was not deployed due to previous parser error(s)")
      } else {
         // memory deployments are for tests only and will all have random origin
         // to avoid schema replacement errors in tests.
         deploy(parserResponse, StringUtils.random64BitString(new Random(System.currentTimeMillis())))
      }
   }
}

/**
 * Deploy single schema from classpath.
 */
class SchemaDeployerClasspath(resource: String)
   extends SchemaDeployerString(IoUtils.toString(IoUtils.openResourceAsStream(resource)._1()))
