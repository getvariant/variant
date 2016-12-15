package com.variant.client;



/**
 * <p>An environment dependent implementation will use an external mechanism to obtain 
 * and to store the session ID between state requests. For instance, in a Web application 
 * environment, session ID should be tracked in an HTTP cookie, just like HTTP session ID.
 * Request scoped, i.e. Variant will reinitialize the concrete implementation class
 * at the start of a state request and destroy it at commit.
 * 
 * <p>Variant maintains its own session, rather than relying on the host application's native
 * session, because 1) some host environments won't have a native session; and 2) it is frequently 
 * desirable for Variant session to survive the destruction of the host application's session. 
 * For example, if the host application is a Web application, 
 * it natively relies on the HTTP session, provided to it by a Web container, like Tomcat. 
 * If a Variant experiment starts on a public page and continues past the login page, 
 * it is possible (in fact, quite likely) that the host application will recreate the 
 * underlying HTTP session upon login. If Variant session were somehow bound to the HTTP session, 
 * it would not be able to span states on the opposite side of the login page. 
 * But because Variant manages its own session, the fate of the host application's HTTP session 
 * is irrelevant, enabling Variant to instrument experiments that start by an unknown 
 * user and end by an authenticated one or vice versa.
 * 
 * <p>By contract, an implementation must provide a no-argument constructor, which Variant will use
 * to instantiate it. To inject state, call {@link #init(VariantInitParams, Object...)}.
 *
 * @author Igor Urisman
 * @since 0.6
 */

public interface VariantSessionIdTracker {

	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation. Variant client calls this method 
	 * immediately following the instantiation within the scope of the {@link VariantClient#getSession(Object...)} method.
	 * Use this to inject state from configuration.
	 * 
	 * @param client    The instance of the Variant client API which is initializing this object.
	 * @param userData  An array of zero or more opaque objects which {@link VariantClient#getSession(Object...)}  
	 *                  or {@link VariantClient#getOrCreateSession(Object...)} method will pass here without 
	 *                  interpretation.
	 * 
	 * @since 0.6
	 */
	public void init(VariantClient client, Object...userData);

	/**
	 * <p>Retrieve the current value of the session ID from the tracker. 
	 * This value may have been set by {@link #init(VariantInitParams, Object...)} or by {@link #set(String)}.
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
	 * Variant client calls this method within the scope of the {@link VariantCoreStateRequest#commit(Object...)} method.
	 * 
	 * @param userData An array of zero or more opaque objects which {@link VariantCoreStateRequest#commit(Object...)}
	 *                 will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	public void save(Object...userData);

}

