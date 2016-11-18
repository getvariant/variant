package com.variant.server;

import com.variant.core.VariantProperties;
import com.variant.server.event.EventFlusherAppLogger;

public class ServerPropertiesKey implements VariantProperties.Key {
	
	public static final ServerPropertiesKey SCHEMAS_DIR =
			new ServerPropertiesKey("variant.schemas.dir");
	
	public static final ServerPropertiesKey SESSION_TIMEOUT =
			new ServerPropertiesKey("variant.session.timeout", 900);
	
	public static final ServerPropertiesKey  SESSION_STORE_VACUUM_INTERVAL =
			new ServerPropertiesKey("variant.session.store.vacuum.interval", 10);
	
	public static final ServerPropertiesKey EVENT_FLUSHER_CLASS_NAME =
			new ServerPropertiesKey("variant.event.flusher.class.name", EventFlusherAppLogger.class.getName());
	
	public static final ServerPropertiesKey EVENT_FLUSHER_CLASS_INIT =
			new ServerPropertiesKey("variant.event.flusher.class.init","{}");
	
	public static final ServerPropertiesKey EVENT_WRITER_PERCENT_FULL = 
			new ServerPropertiesKey("event.writer.percent.full", 50);
	
	public static final ServerPropertiesKey EVENT_WRITER_BUFFER_SIZE = 
			new ServerPropertiesKey("variant.event.writer.buffer.size", 20000);
	
	public static final ServerPropertiesKey EVENT_WRITER_MAX_DELAY = 
			new ServerPropertiesKey("variant.event.writer.max.delay", 30000);
	
	private final String name;
	private final Object defaultValue;
	
	/**
	 * Key with default
	 */
   	private ServerPropertiesKey(String name, Object defaultValue) {
   		this.name = name;
   		this.defaultValue = defaultValue;
   	}

   	/**
   	 * Key without default
   	 */
   	private ServerPropertiesKey(String name) {
   		this.name = name;
   		this.defaultValue = null;
   	}

   	@Override
	public String getExternalName() {
		return name;
	}

	@Override
	public Object getDefault() {
		return defaultValue;
	}

}
