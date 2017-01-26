package com.variant.client;



/**
 * <p>An environment-bound implementation will use an external mechanism 
 * to store the current session ID between state requests. For instance, in a Web application 
 * environment, session ID should be tracked in an HTTP cookie, just like the HTTP session ID.
 * Request scoped, i.e. Variant will reinitialize the concrete implementation class
 * at the start of a state request and destroy it at commit.
 * 
 * <p>By contract, an implementation must provide a no-argument constructor, which Variant will use
 * to instantiate it. To inject initial state, see {@link #init(Connection, Object...)}.
 *
 * @author Igor Urisman
 * @since 0.6
 */

public interface SessionIdTracker {

	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation 
	 * immediately following the instantiation. Called within the scope of the {@code Connection.getSession()} methods.
	 * 
	 * @param conn      The Variant server connection which is initializing this object.
	 * @param userData  An array of zero or more opaque objects, which the enclosing call to {@link Connection#getSession(Object...) }
	 *                  or {@link Connection#getOrCreateSession(Object...)} will pass here without interpretation. 
	 * 
	 * @since 0.6
	 */
	public void init(Connection conn, Object...userData);

	/**
	 * <p>Retrieve the current value of the session ID from the tracker. 
	 * This value may have been set by {@link #init(Connection, Object...)} or by {@link #set(String)}.
	 * 
	 * @return Session ID, if present in the tracker or null otherwise.
	 * @since 0.6
	 */
	public String get();
	
	/**
	 * <p>Set the value of session ID. Use to start tracking a new session.
	 * 
	 * @param sessionId Session ID to set.
	 * @since 0.6
	 */
	public void set(String sessionId);

	/**
	 * <p>Called by Variant to save the current value of session ID to the underlying persistence mechanism. 
	 * Variant client calls this method within the scope of the {@link StateRequest#commit(Object...)} method.
	 * 
	 * @param userData An array of zero or more opaque objects which {@link StateRequest#commit(Object...)}
	 *                 will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	public void save(Object...userData);

}

