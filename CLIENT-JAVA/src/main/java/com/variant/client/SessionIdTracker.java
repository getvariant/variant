package com.variant.client;

/**
 * Interface to be implemented by a session ID tracker. Variant does its own session management,
 * but relies on the host application to track session IDs between state requests. The concrete
 * implementation depend on the technology stack of the host application. For example, a web
 * application will use an HTTP cookie based tracker, just like
 * <a href="https://github.com/getvariant/variant-java-servlet-adapter/blob/master/src/main/java/com/variant/client/servlet/SessionIdTrackerHttpCookie.java" target="_blank">SessionIdTrackerHttpCookie</a>
 * included in the
 * <a href="https://github.com/getvariant/variant-java-servlet-adapter" target="_blank">Servlet Adapter for Variant Java Client</a>.
 * <p>
 * The implementation has a request-scoped lifecycle, i.e. Variant re-instantiates the 
 * implementing class at the start of each state request. By contract, an implementation must 
 * provide the constructor with the signature <code>ImplClassName(Object...)</code>.
 * Variant uses this constructor to instantiate a concrete implementation within the scope 
 * of {@link Connection#getSession(Object...)} or {@link Connection#getOrCreateSession(Object...)} 
 * methods by passing it these arguments without interpretation.
 * <p>
 * Configured with the {@link VariantClient.Builder#withSessionIdTrackerClass(Class)}.  There is no default implementation.
 *
 * @since 0.6
 */

public interface SessionIdTracker {

	/**
	 * The current value of the session ID tracked by this object.
	 * 
	 * @since 0.6
	 */
	public String get();
	
	/**
	 * Set the value of session ID tracked by this object.
	 * 
	 * @since 0.6
	 */
	public void set(String sessionId);

	/**
	 * Save the value of session ID, tracked by this object, to the underlying persistence mechanism. 
	 * Variant client calls this method within the scope of the {@link StateRequest#commit(Object...)}
	 * or {@link StateRequest#fail(Object...)} methods.
	 * 
	 * @param userData An array of zero or more opaque objects which {@link StateRequest#commit(Object...)}
	 *                or {@link StateRequest#fail(Object...)} will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	public void save(Object...userData);

}

