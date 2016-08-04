package com.variant.client;

import com.variant.core.VariantStateRequest;


/**
 * <p>An implementation will use external mechanisms to obtain and to store
 * the session ID between state requests. For instance, in a Web application environment, 
 * session ID should be tracked in an HTTP cookie, just like HTTP session ID.
 * Request scoped, i.e. Variant will reinitialize the concrete implementation class
 * at the start of a state request and destroy it at commit.
 * 
 * <p>Variant maintains its own session, rather than relying on the host application’s, 
 * because it is frequently desirable for Variant session to survive the destruction 
 * of the host application’s session. For example, if the host application is a Web application, 
 * it natively relies on the HTTP session, provided to it by a Web container, like Tomcat. 
 * If a Variant experiment starts on a public page and continues past the login page, 
 * it is possible (in fact, quite likely) that the host application will recreate the 
 * underlying HTTP session upon login. If Variant session were somehow bound to the HTTP session, 
 * it would not be able to span states on the opposite side of the login page. 
 * But because Variant manages its own session, the fate of the host application’s HTTP session 
 * is irrelevant, enabling Variant to instrument experiments that start by an unknown 
 * user and end by an authenticated one or vice versa.
 * 
 * @author Igor Urisman
 * @since 0.6
 */

public interface VariantSessionIdTracker {

	/**
	 * <p>Called by Variant client immediately following the instantiation within the scope of the
	 * {@link VariantClient#getSession(Object...userData)} method.</p>
	 * 
	 * @param initParams The init parameter map, as specified by the <code>targeting.tracker.class.init</code>
	 *                   application property. 
	 * @param userData   An array of 0 or more opaque objects which {@link VariantClient#getSession(Object...userData)}  
	 *                   will pass here without interpretation.
	 * 
	 * @since 0.6
	 */
	public void init(VariantInitParams initParams, Object...userData);

	/**
	 * <p>Retrieve the current value of the session ID from the tracker. This value may have been set by <code>init()</code>
	 * or by the <code>set()</code></p>
	 * 
	 * @return Session ID, if present in the tracker or null otherwise.
	 * @since 0.6
	 */
	public String get();
	
	/**
	 * <p>Set the value of session ID.</p>
	 * 
	 * @return Session ID, if present in the tracker or null otherwise.
	 * @since 0.6
	 */
	public void set(String sessionId);

	/**
	 * <p>Save the current value of session ID to the underlying persistence mechanism.</p>
	 * 
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#commitStateRequest(VariantStateRequest, Object...)} 
	 *                 will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	public void save(Object...userData);

}

