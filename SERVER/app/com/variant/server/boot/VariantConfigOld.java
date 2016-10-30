package com.variant.server.boot;

import java.util.List;

import com.variant.server.event.EventFlusherAppLogger;

import play.api.Configuration;
import scala.Option;

/**
 * Wrap Play provided applicaiton configuration in 
 * Variant specific adaptor.
 *
public class VariantConfig {
  
	private final Configuration playConfig;
	
	VariantConfig(Configuration playConfig) {
		this.playConfig = playConfig;
	}
	
    private String get(Key key) {
    	Option<List<String>> result = playConfig.getStringList(key.name);
    	return (result.isEmpty() || result.get().isEmpty()) ? key.defaultValue.toString() : result.get().get(0);
    }

    public String getString(Key key) {
    	return get(key);
    }
    
    public int getInt(Key key) {
    	return Integer.parseInt(get(key));
    }

    public long getLong(Key key) {
    	return Long.parseLong(get(key));
    }

    public enum Key {

    	SessionTimeoutSecs("variant.session.timeout.secs", 900),
    	SessionStoreVacuumIntervalSecs("variant.session.store.vacuum.interval.secs", 10),
    	EventFlusherClassName("variant.event.flusher.class.name", EventFlusherAppLogger.class.getName()),
    	EventFlusherClassInit("variant.event.flusher.class.init","{}"),
    	EventWriterPercentFull("event.writer.percent.full", 50),
    	EventWriterBufferSize("variant.event.writer.buffer.size", 20000),
    	EventWriterFlushMaxDelayMillis("variant.event.writer.flush.max.delay.millis", 30000);
    	
    	private String name;
    	private Object defaultValue;
    	
       	private Key(String name, Object defaultValue) {
       		this.name = name;
       		this.defaultValue = defaultValue;
       	}

	}

}
*/