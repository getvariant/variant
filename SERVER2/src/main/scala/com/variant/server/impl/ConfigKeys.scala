package com.variant.server.impl;

trait ConfigKeys {

   // Public keys
   val SCHEMATA_DIR = "schemata.dir";
   val SESSION_TIMEOUT = "session.timeout";
   val SESSION_VACUUM_INTERVAL = "session.vacuum.interval";
   val EVENT_FLUSHER_CLASS_NAME = "event.flusher.class.name";
   val EVENT_FLUSHER_CLASS_INIT = "event.flusher.class.init";
   val EVENT_WRITER_BUFFER_SIZE = "event.writer.buffer.size";
   val EVENT_WRITER_MAX_DELAY = "event.writer.max.delay";
   val HTTP_PORT = "http.port";

   // Secret keys
   // If this param set to true, all server responses with contain the timing header.
   val WITH_TIMING = "with.timing";

}
