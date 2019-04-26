package com.variant.client;

/**
 * Interface to be implemented by an environment-bound session ID tracker. The implementation will 
 * use an external mechanism to store the current session ID between state requests. 
 * For instance, in a Web application environment, session ID should be tracked in an HTTP cookie, 
 * just like the HTTP session ID.
 * <p>
 * The implementation will have request scoped life-cycle, i.e. Variant will re-instantiate the 
 * implementing class at the start of each state request.By contract, an implementation must 
 * provide the constructor with the following signature <code>ImplClassName(Object...)</code>.
 * Variant will use this constructor to instantiate a concrete implementation within the scope 
 * of {@link Connection#getSession(Object...)} or {@link Connection#getOrCreateSession(Session, Object...)} 
 * methods by passing it these arguments without interpretation.
 * <p>
 * Configured by <code>session.id.tracker.class.name</code> configuration property.
 *
 * @since 0.6
 */

public interface SessionIdTracker {

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

