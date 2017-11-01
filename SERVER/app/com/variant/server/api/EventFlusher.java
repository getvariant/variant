package com.variant.server.api;

import java.util.Collection;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;

/**
 * <p>Interface to be implemented by a user-supplied class, which handles writing
 * Variant events to external storage. The implementation will be instantiated
 * by Variant and is bound to a particular Variant schema. The implementation is defined
 * by the {@code meta/flusher} schema property. If no flusher is supplied in the schema,
 * Variant will used the instance-wide default defined by {@code event.writer.class.name} system property.
 * 
 * 
 * <p>By contract, the implementation must provide a no-argument constructor, which Variant will use
 * to instantiate it. Immediately following instantiation, Variant will call {@link #init(ConfigObject)}
 * to allow for state injection from external configuration, if provided.
 * 
 * <p>The implementation should expect Variant 
 * server to periodically call {@link #flush(Collection)} with a collection of events readu to be
 * flushed. The frequency of this call and the likely number of events in the collection depend
 * on the rate of event production and the following system properties:
 * <ul>
 * <li>{@code variant.event.writer.buffer.size}: The maximum number of pending events that can be
 * be held in event buffer by the asynchronous event writer. (Default = 20,000).
 * <li>{@code variant.event.writer.max.delay}: The maximum delay a pending event will be
 * held in memory. Event writer will flush pending events even if their number has not reached
 * percent full, if the last flush completed this many milliseconds ago. (Default = 30).
 *  </ul>
 * 
 * <p>An implementation must provide at least one public constructor: 
 * <ol>
 * <li>If no state initialization is required, the default nullary constructor is sufficient. 
 * <li>If you need to pass an initial state to the newly constructed hook object, you must provide a constructor
 * which takes the single argument of the type {@link Config}. Variant will invoke this constructor and pass it
 * the value of the hook definitions's {@code init} property.
 * </ol>
 * 
 * <p>Variant creates a new instance of the implementation class for each schema where it is defined.

 * @author Igor Urisman
 * @since 0.7
 */
public interface EventFlusher {
	
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
