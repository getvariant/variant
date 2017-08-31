package com.variant.server.api;

import java.util.Collection;

import com.typesafe.config.ConfigObject;

/**
 * <p>Interface to be implemented by a user-supplied class, which handles writing
 * Variant events to external storage. This implementation will be instantiated
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
 * <li>{@code variant.event.writer.buffer.size}: The maximum number of pending events that can be
 * be held in event buffer by the asynchronous event writer. (Default = 20,000).
 * <li>{@code variant.event.writer.percent.full}: How full is the event buffer allowed to fill up
 * before it is flushed.(Default = 50, i.e. event writer will flush
 * when the event buffer becomes half-full.) ,
 * <li>{@code variant.event.writer.max.delay.millis}: The maximum delay a pending event will be
 * held in memory. Event writer will flush pending events even if their number has not reached
 * percent full, if the last flush completed this many milliseconds ago. (Default = 30,000).
 *  </ul>
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public interface EventFlusher {

	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation. 
	 * Variant server calls this method immediately following the instantiation during 
	 * Variant server's initialization. Use this to inject state from external configuration.
	 * 
	 * @param configObject The object of type {@link ConfigObject} holding the parsed
	 *                     HOCON value given by the variant.event.flusher.class.init configuration
	 *                     parameter.
	 * 
	 * @since 0.7
	 */
	public void init(ConfigObject config) throws Exception;
	
	/**
	 * <p>Called by the server, whenever the asynchronous event writer needs to flush events from memory. 
	 * 
	 * @param events A collection of decorated variant events {@link FlushableEvent} to be written off.
	 *               The size of the collection may be up to the size defined by the 
     *               variant.event.writer.buffer.size configuration property.
	 * 
	 * @see EventFlusherH2, EventFlusherPostgres
	 * @since 0.7
	 */
	public void flush(Collection<FlushableEvent> events) throws Exception;

}
