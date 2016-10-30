package com.variant.server.boot;

import com.variant.server.event.EventFlusherAppLogger;

public enum VariantConfigKey {
	
	SessionTimeoutSecs("variant.session.timeout.secs", 900),
	SessionStoreVacuumIntervalSecs("variant.session.store.vacuum.interval.secs", 10),
	EventFlusherClassName("variant.event.flusher.class.name", EventFlusherAppLogger.class.getName()),
	EventFlusherClassInit("variant.event.flusher.class.init","{}"),
	EventWriterPercentFull("event.writer.percent.full", 50),
	EventWriterBufferSize("variant.event.writer.buffer.size", 20000),
	EventWriterFlushMaxDelayMillis("variant.event.writer.flush.max.delay.millis", 30000);
	
	public final String name;
	public final Object defaultValue;
	
   	private VariantConfigKey(String name, Object defaultValue) {
   		this.name = name;
   		this.defaultValue = defaultValue;
   	}

}
