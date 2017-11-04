package com.variant.server.api;

/**
  * Known configuration keys.
  * @author Igor Urisman
  * @since 0.7
  */
public interface ConfigKeys {
	
	public static final String SCHEMATA_DIR               = "variant.schemata.dir";	
	public static final String MAX_CONCURRENT_CONNECTIONS =  "variant.max.concurrent.connections";
	public static final String SESSION_TIMEOUT            = "variant.session.timeout";
	public static final String SESSION_VACUUM_INTERVAL    =  "variant.session.vacuum.interval";
	public static final String EVENT_FLUSHER_CLASS_NAME   = "variant.event.flusher.class.name";
	public static final String EVENT_FLUSHER_CLASS_INIT   = "variant.event.flusher.class.init";
	public static final String EVENT_WRITER_BUFFER_SIZE   =  "variant.event.writer.buffer.size";
	public static final String EVENT_WRITER_MAX_DELAY     = "variant.event.writer.max.delay";

}
