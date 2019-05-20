package com.variant.server.api;

import java.util.Optional;

/**
 * Variant Server's runtime configuration currently in effect. Can be obtained by calling {@link Session#getConfiguration()}.
 * 
 * @since 0.10
 */
public interface Configuration {
 
	/**
	 * The schemata directory where Variant server is looking for variation schemata. 
	 * Provided by the <code>variant.schemata.dir</code> configuration property. Cannot be null.
	 * 
	 * @since 0.10
	 */
	String getSchemataDir();

	/**
	 * Session timeout in seconds. Provided by the <code>variant.session.timeout</code> configuration property.
	 * Idle session are periodically cleaned out by the session vacuum. 
	 * 
	 * @since 0.10
	 */
	int getSessionTimeout();

	/**
	 * Session vacuum interval in seconds. Provided by the <code>variant.session.timeout.interval</code> configuration property.
	 * 
	 * @since 0.10
	 */
	int getSessionVacuumInterval();

	/**
	 * System-wide default trace event flusher. Provided by the <code>variant.event.flusher.class.name</code> configuration property.
	 * Normally, each schema defines its own event flusher in the <code>/meta/flusher</code> element. If omitted, Variant
	 * server will default to this implementation. Cannot be null.
	 * 
	 * @since 0.10
	 */
	String getEventFlusherClassName();

	/**
	 * Initial state for the default trace event flusher. Provided by the <code>variant.event.flusher.class.init</code> configuration property.
	 * Arbitrary JSON object.
	 * 
	 * @since 0.10
	 */
	Optional<String> getEventFlusherClassInit();

	/**
	 * Event writer buffer size. Provided by the <code>variant.event.writer.buffer.size</code> configuration property.
	 * Event writer will be able to temporarily hold in memory up to this many generated trace events.
	 * 
	 * @since 0.10
	 */
	int getEventWriterBufferSize();

	/**
	 * Event writer maximum delay in seconds. Provided by the <code>variant.event.writer.max.delay</code> configuration property.
	 * Normally, event writer is woken up when its buffer is 50 percent full. For testing or demo purposes, you may override this
	 * behavior by limiting the time event writer may take to write our pending trace events.
	 * 
	 * @since 0.10
	 */
	int getEventWriterMaxDelay();
	
	/**
	 * Network port bound by Variant server. Provided by the <code>-Dhttp.port</code> system property.
	 * 
	 * @since 0.10
	 */
	String getNetworkPort();
}