package com.variant.server.impl;

public interface ConfigKeys {

	// Public keys
	public final String SCHEMATA_DIR               = "schemata.dir";
	public final String SESSION_TIMEOUT            = "session.timeout";
	public final String SESSION_VACUUM_INTERVAL    = "session.vacuum.interval";
	public final String EVENT_FLUSHER_CLASS_NAME   = "event.flusher.class.name";
	public final String EVENT_FLUSHER_CLASS_INIT   = "event.flusher.class.init";
	public final String EVENT_WRITER_BUFFER_SIZE   = "event.writer.buffer.size";
	public final String EVENT_WRITER_MAX_DELAY     = "event.writer.max.delay";
	public final String HTTP_PORT                  = "http.port";

	// Secret keys
	// If this param set to true, all server responses with contain the timing header.
	public final String WITH_TIMING = "with.timing";

}
