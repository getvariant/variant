package com.variant.server.impl;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator

trait ConfigKeys {

   private val PREFIX = "variant."
  
   // Public keys
   val EVENT_FLUSHER_CLASS_NAME = PREFIX + "event.flusher.class.name";
   val EVENT_FLUSHER_CLASS_INIT = PREFIX + "event.flusher.class.init";
   val EVENT_FLUSH_SIZE = PREFIX + "event.writer.flush.size";
   val EVENT_WRITER_BUFFER_SIZE = PREFIX + "event.writer.buffer.size";
   val EVENT_WRITER_MAX_DELAY = PREFIX + "event.writer.max.delay";
   val HTTP_PORT = PREFIX + "http.port";
   val HTTPS_PORT = PREFIX + "https.port";
   val SCHEMATA_DIR = PREFIX + "schemata.dir";
   val SESSION_TIMEOUT = PREFIX + "session.timeout";
   val SESSION_VACUUM_INTERVAL = PREFIX + "session.vacuum.interval";

   // Secret keys
   // If this param set to true, all server responses with contain the timing header.
   val WITH_TIMING = PREFIX + "with.timing";

}
