package com.variant.server.api;

import java.util.Collection;

/**
 * <p>Interface to be implemented by a user-supplied class, handling the writing of
 * trace events to external storage. The implementation will be instantiated
 * by Variant and is bound to a particular Variant schema. The implementation is defined
 * by the <code>/meta/flusher</code> schema element. If no flusher is supplied in the schema,
 * Variant will use the server-wide default, as provided by 
 * {@code variant.event.flusher.class.name} and {@code variant.event.flusher.class.init} 
 * configuration properties.
 * 
 * <p>The implementation should expect Variant 
 * server to periodically call {@link #flush(Collection)} with a collection of {@link FlushableTraceEvent}s,
 * ready to be flushed to external storage. The frequency of this call and the likely number of events in the collection depend
 * on the rate of event production and the following system properties:
 * <ul>
 * <li>{@code variant.event.writer.buffer.size}: The maximum number of pending events that can be
 * held in memory by the event writer. (Default = 20,000). If the rate of event production is too
 * high and the event flusher can't keep up, new events will be discarded with the appropriate error
 * message in the server's log.
 * <li>{@code variant.event.writer.max.delay}: The maximum delay, in seconds, a pending event will be
 * held in memory. (Default = 30) If the rate of event production is too low, pending events may be
 * stuck in memory for too long, risking being lost in the event of a server crash, or delaying a critical
 * action further down the data pipeline. This property will force the event writer to flush pending
 * events even if a large portion of the event buffer is still free.
 *  </ul>
 * <p>
 * An implementation must provide at least one public constructor: 
 * <ol>
 * <li>If no state initialization is required, i.e. the {@code variant.event.flusher.class.init} is <code>null</code>,
 * the nullary constructor is sufficient.
 * <li>If you need to pass an initial state to the newly constructed event flusher object, the implementation must provide
 * a constructor which takes the single argument of type <a href="https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html" target="_blank">com.typesafe.config.Config</a>.
 * Variant will invoke this constructor and pass it the parsed value of the {@code variant.event.flusher.class.init} 
 * property.
 * </ol>
 * 
 * <p>Variant creates a new instance of the implementation class for each schema where it is defined.

 * @author Igor Urisman
 * @since 0.7
 */
public interface TraceEventFlusher {
	
	/**
	 * <p>Called by the server whenever a new collection of trace events become available to be flushed out of memory.
	 * 
	 * @param events An array of decorated trace event objects of type {@link FlushableTraceEvent} to be flushed to an external
	 *               data sync. The array length is guaranteed to be as given by the {@code variant.event.writer.flush.size},
	 *               although only first {@code size} elements are usable.
	 * @param size   The number of elements in the events array. Most of the time size will be equal to the length of the
	 *               {@code events} array. But whenever the events production is slow enough for the 
	 *               {@code variant.event.writer.max.delay} config parameter to become applicable, partial flushes may be
	 *               trigger by the server. 
	 * 
	 * @since 0.7
	 */
	void flush(FlushableTraceEvent[] events, int size) throws Exception;
	
   /**
    * <p>Called by the server, whenever this event flusher is being destroyed because the associated schema generation is being undeployed.
    * Client code can use this callback to do housekeeping tasks such as closing database connections.
    * 
    * @since 0.10
    */
   default void destroy() throws Exception {}

}
