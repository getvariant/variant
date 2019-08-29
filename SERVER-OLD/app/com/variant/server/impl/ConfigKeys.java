package com.variant.server.impl;

public interface ConfigKeys {

	// Public keys
	public final String SCHEMATA_DIR               = "variant.schemata.dir";
	public final String SESSION_TIMEOUT            = "variant.session.timeout";
	public final String SESSION_VACUUM_INTERVAL    = "variant.session.vacuum.interval";
	public final String EVENT_FLUSHER_CLASS_NAME   = "variant.event.flusher.class.name";
	public final String EVENT_FLUSHER_CLASS_INIT   = "variant.event.flusher.class.init";
	public final String EVENT_WRITER_BUFFER_SIZE   = "variant.event.writer.buffer.size";
	public final String EVENT_WRITER_MAX_DELAY     = "variant.event.writer.max.delay";

	// Secret keys
	// If this param set to true, all server responses with contain the timing header.
	public final String WITH_TIMING = "variant.with.timing";

}
