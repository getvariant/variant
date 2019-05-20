/**
 * Top level classes in the Variant Server Extension SPI.
 * User code consuming this API runs in the address space of Variant Server.
 * <p>
 * There are two principal types of server-side extensions which can be developed
 * using this SPI: lifecycle hooks and event flushers. Lifecycle hooks are
 * listeners of lifecycle events, e.g. descendants of the {@link com.variant.server.api.lifecycle.LifecycleEvent},
 * type. Lifecycle hooks provide a convenient way of injecting application specific semantics in 
 * Variant server's default behavior.
 * <p>
 * Event flushers are responsible for final ingestion of Variant trace events (not to be
 * confused with lifecycle events). The <a href="https://github.com/getvariant/variant-extapi-standard" target="_blank">standard extension library</a>, 
 * which comes with Variant Server, contains several popular event flushers, such as one for the PostgreSQL database.
 * It is likely, however, that you will want to develop your own flusher to suit your application environment.
 * 
 *
 * @since 0.7
 */
package com.variant.server.api;
