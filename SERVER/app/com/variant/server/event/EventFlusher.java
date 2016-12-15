package com.variant.server.event;

import java.util.Collection;

import com.variant.server.ServerProperties;

/**
 * <p>An environment dependent implementation will use external mechanisms to flush a bunch
 * of pending Variant events to external storage. The implementation will be instantiated
 * by Variant and must be supplied in the {@code event.writer.class.name} system property.
 * By contract, the implementation must provide a no-argument constructor, which Variant will use
 * to instantiate it. Immedately followign instantiation, Variant will call {@link #init(VariantCoreInitParams)}
 * to allow state injection from external configuration.
 * 
 * <p>The implementation should expect Variant 
 * server to periodically call {@link #flush(Collection)} with a collection of events to be
 * written off. The frequency of this call and the likely number of passed events depend
 * on the rate of event production and the following system properties:
 * <ul>
 * <li>{@code event.writer.buffer.size}: The maximum number of pending events that can be
 * be held in event buffer by the asynchronous event writer. (Default = 20,000).
 * <li>{@code event.writer.percent.full}: How full is the event buffer allowed to fill up
 * before it is flushed.(Default = 50, i.e. event writer will flush
 * when the event buffer becomes half-full.) ,
 * <li>{@code event.writer.max.delay.millis}: The maximum delay a pending event will be
 * held in memory. Event writer will flush pending events even if their number has not reached
 * percent full, if the last flush completed this many milliseconds ago. (Default = 30,000).
 *  </ul>
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface EventFlusher {

	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation. 
	 * Variant server calls this method immediately following the instantiation while 
	 * initializing Variant Server. Use this to inject state from configuration.
	 * 
	 * @param config Server configuration. 
	 * 
	 * @since 0.5
	 */
	public void init(ServerProperties config) throws Exception;
	
	/**
	 * <p>Flush a bunch of events to external storage.
	 * 
	 * @param events A collection of decorated variant events {@link FlushableEvent} to be written off.
	 * 
	 * @see EventFlusherH2, EventFlusherPostgres
	 * @since 0.5
	 */
	public void flush(Collection<FlushableEvent> events) throws Exception;

}
