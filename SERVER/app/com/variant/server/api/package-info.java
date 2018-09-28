/**
 * This package contains top level classes for Variant Server Extension API.
 * User code taking advantage of this API runs in the address space of
 * Variant Experience Server.
 * <p>
 * There are two principal types of server-side extensions which can be developed
 * using this API: life-cycle hooks and event flushers. Life-cycle hooks are
 * listeners to life-cycle events, e.g. the {@link com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent},
 * which is raised whenever a session's qualification for a variation must be established.
 * Life-cycle hooks provide a convenient way to augment Variant server's default behavior with
 * custom semantics.
 * <p>
 * Event flushers are responsible for writing individual trace events (not to be
 * confused with life-cycle events) to persistent storage. Variant server comes with
 * a few basic event flushers, such as one for the PostgreSQL database, but you will
 * likely want to develop your own flusher to suit your application environment. 
 * 
 *
 * @since 0.7
 */
package com.variant.server.api;
