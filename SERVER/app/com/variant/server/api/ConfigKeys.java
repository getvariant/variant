package com.variant.server.api;

/**
 * Known external configuration keys. These are compile time aliases for the
 * configuration keys, used in the variant.conf file. For example, to obtain
 * the effective value of session time-out call ...
 * TODO: Expose server config at run time (#131)
 * 
 * @since 0.7
 */
public interface ConfigKeys {
	
	public static final String SCHEMATA_DIR               = "variant.schemata.dir";	
	public static final String SESSION_TIMEOUT            = "variant.session.timeout";
	public static final String SESSION_VACUUM_INTERVAL    = "variant.session.vacuum.interval";
	public static final String EVENT_FLUSHER_CLASS_NAME   = "variant.event.flusher.class.name";
	public static final String EVENT_FLUSHER_CLASS_INIT   = "variant.event.flusher.class.init";
	public static final String EVENT_WRITER_BUFFER_SIZE   = "variant.event.writer.buffer.size";
	public static final String EVENT_WRITER_MAX_DELAY     = "variant.event.writer.max.delay";
	
}
