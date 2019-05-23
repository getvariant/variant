package com.variant.client;

import java.util.Map;


/**
 * Variant trace event. Trace events can be triggered explicitly by the client code or implicitly by Variant server.
 * Explicit trace events are triggered by {@link Session#triggerTraceEvent(com.variant.core.TraceEvent)}. Implicit trace events
 * are generated automatically by Variant server. All trace events are processed by Variant Server, outside of the host application.
 * 
 * @since 0.5
 */
public interface TraceEvent {

	/**
	 * The name of the event.
	 *
	 * @since 0.5
	 */
	String getName();
		
	/**
	 * The map of event attributes.
	 * 
	 * @return A mutable map of currently set events attributes.  Can be manipulated by client code.
	 * 
	 * @since 0.5
	 */
	Map<String,String> getAttributes();
	
}
