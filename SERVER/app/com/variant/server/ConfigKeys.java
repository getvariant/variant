package com.variant.server;

public interface ConfigKeys {
	
	public static final String SCHEMAS_DIR =                   "schemas.dir";	
	public static final String MAX_CONCURRENT_CONNECTIONS =    "max.concurrent.connections";
	public static final String SESSION_TIMEOUT =               "session.timeout";
	public static final String SESSION_STORE_VACUUM_INTERVAL = "session.store.vacuum.interval";
	public static final String EVENT_FLUSHER_CLASS_NAME =      "event.flusher.class.name";
	public static final String EVENT_FLUSHER_CLASS_INIT =      "event.flusher.class.init";
	public static final String EVENT_WRITER_PERCENT_FULL =     "writer.percent.full";
	public static final String EVENT_WRITER_BUFFER_SIZE =      "event.writer.buffer.size";
	public static final String EVENT_WRITER_MAX_DELAY =        "event.writer.max.delay";

}
