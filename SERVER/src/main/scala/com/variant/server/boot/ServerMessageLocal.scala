package com.variant.server.boot;

import com.variant.share.error.UserError;
import com.variant.share.error.UserError.Severity;

/**
 * Local Server messages, i.e. the ones emitted to the server log and not sent back to client.
 * These are raised by UserErrorException.
 */
class ServerMessageLocal private (code: Int, severity: Severity, format: String) extends UserError(code, severity, format)

object ServerMessageLocal {

   //
   // 201-220              Configuration
   //
   val CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN =
      new ServerMessageLocal(201, Severity.FATAL, "Cannot set both [variant.config.resource] and [variant.config.file] system parameters");

   val CONFIG_RESOURCE_NOT_FOUND =
      new ServerMessageLocal(202, Severity.FATAL, "Config resource [%s] could not be found");

   val CONFIG_FILE_NOT_FOUND =
      new ServerMessageLocal(203, Severity.FATAL, "Config file [%s] could not be found");

   val CONFIG_PROPERTY_NOT_SET =
      new ServerMessageLocal(204, Severity.FATAL, "Required configuration property [%s] is not set");

   val CONFIG_PROPERTY_WRONG_TYPE =
      new ServerMessageLocal(205, Severity.FATAL, "Configuration property [%s] must be of type [%s] but was of type [%s]");

   //
   // 401-420 Server bootstrap / shutdown
   //
   val SCHEMATA_DIR_MISSING =
      new ServerMessageLocal(401, Severity.FATAL, "Schemata deploy directory [%s] does not exist");

   val SCHEMATA_DIR_NOT_DIR =
      new ServerMessageLocal(402, Severity.FATAL, "Schemata deploy directory [%s] exists but is not a directory");

   val EVENT_FLUSHER_NO_INTERFACE =
      new ServerMessageLocal(403, Severity.FATAL, "Event flusher class [%s] must implement interface [%s]");

   val EVENT_BUFFER_CACHE_SHUTDOWN_TIMEOUT =
      new ServerMessageLocal(404, Severity.ERROR, "Timed out after [%s] milliseconds waiting for event buffer cache to flush");

   //
   // 421-440 Schema deployment
   //
   val SCHEMA_DEPLOYING =
      new ServerMessageLocal(421, Severity.INFO, "Deploying schema from file [%s]");

   val SCHEMA_DEPLOYED =
      new ServerMessageLocal(422, Severity.INFO, "Deployed schema [%s] from file [%s]");

   val SCHEMA_CANNOT_REPLACE =
      new ServerMessageLocal(423, Severity.ERROR, "Cannot replace existing schema [%s] defined in file [%s] with another schema with the same name defined in file [%s]");

   val OBJECT_CONSTRUCTOR_ERROR =
      new ServerMessageLocal(424, Severity.ERROR, "Unable to instantiate class [%s] becuase no suitable constructor was found");

   val OBJECT_INSTANTIATION_ERROR =
      new ServerMessageLocal(425, Severity.ERROR, "Unable to instantiate class [%s] due to error [%s]");

   val HOOK_CLASS_NO_INTERFACE =
      new ServerMessageLocal(426, Severity.ERROR, "Lifecycle hook class [%s] must implement interface [%s]");

   val HOOK_STATE_SCOPE_VIOLATION =
      new ServerMessageLocal(427, Severity.ERROR, "Lifecycle hook defined at [%s], cannot listen to lifecycle event [%s]");

   val HOOK_VARIATION_SCOPE_VIOLATION =
      new ServerMessageLocal(428, Severity.ERROR, "Lifecycle hook defined at [%s], cannot listen to lifecycle event [%s]");

   /*	val FLUSHER_NOT_CONFIGURED =
			new ServerMessageLocal(429, Severity.ERROR, "No event flusher defined in experiment schema, and no default event flusher is configured");
*/
   val FLUSHER_CLASS_NO_INTERFACE =
      new ServerMessageLocal(430, Severity.ERROR, "Event flusher class [%s] must implement interface [%s]");

   val SCHEMA_FAILED =
      new ServerMessageLocal(431, Severity.WARN, "Schema [%s] was not deployed from [%s] due to previous errors");

   val EMPTY_SCHEMATA =
      new ServerMessageLocal(432, Severity.INFO, "No schemata found in [%s]");

   val SERVER_BOOT_OK =
      new ServerMessageLocal(433, Severity.INFO, "%s started on port [%s] in %s");

   val SERVER_BOOT_FAILED =
      new ServerMessageLocal(434, Severity.INFO, "%s failed to bootstrap due to following error(s)");

   val SERVER_SHUTDOWN =
      new ServerMessageLocal(435, Severity.INFO, "%s shutdown on  port [%s] in [%s], uptime %s.");

   //
   // 501-520 Misc Runtime
   //
   val TRASHED_EVENTS_COUNT =
      new ServerMessageLocal(501, Severity.WARN, "Trashed [%s] trace events in the last [%s]. Consider increasing variant.event.writer.buffer.size");

   val FLUSHER_CLIENT_ERROR =
      new ServerMessageLocal(502, Severity.ERROR, "Trace event flusher thew an exception");

}

