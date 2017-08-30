package com.variant.server;

/**
  * Known configuration keys.
  * @author Igor Urisman
  * @since 0.7
  */
public interface ConfigKeys {
	
	public static final String SCHEMAS_DIR =                   "variant.schemas.dir";	
	public static final String MAX_CONCURRENT_CONNECTIONS =    "variant.max.concurrent.connections";
	public static final String SESSION_TIMEOUT =               "variant.session.timeout";
	public static final String SESSION_STORE_VACUUM_INTERVAL = "variant.session.store.vacuum.interval";
	public static final String EVENT_FLUSHER_CLASS_NAME =      "variant.event.flusher.class.name";
	public static final String EVENT_FLUSHER_CLASS_INIT =      "variant.event.flusher.class.init";
	public static final String EVENT_WRITER_PERCENT_FULL =     "variant.event.writer.percent.full";
	public static final String EVENT_WRITER_BUFFER_SIZE =      "variant.event.writer.buffer.size";
	public static final String EVENT_WRITER_MAX_DELAY =        "variant.event.writer.max.delay";

}