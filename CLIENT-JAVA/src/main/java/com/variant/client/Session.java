package com.variant.client;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.typesafe.config.Config;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * <p>Represents a Variant user session. Provides a way to 
 * identify a user across multiple state requests and contains session-scoped application 
 * state that must be preserved between state requests. Variant server acts as the session 
 * store by maintaining a map of user session objects keyed by session ID.
 * 
 * <p>Variant maintains its own session, instead of 
 * relying on the host application's native session, because 1) some host environments 
 * won't have the notion of a native session; and 2) it is frequently 
 * desirable for a Variant session to survive the destruction of the host application's session. 
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
 * <p> Variant session expires when either a configurable session timeout period of inactivity,
 *     or after a schema re-deployment. Once the session represented a Session object has expired,
 *     all subsequent operations on it, apart from {@code getId()} and {@code isExpired()} will throw 
 *     a {@link SessionExpiredException}.
 *
 * @author Igor Urisman
 * @since 0.5
 *
 */
public interface Session {

	/**
     * <p>This session's ID.
     *  
	 * @return Session ID.  
	 *
	 * @since 0.7
	 */
	String getId();

	/**
     * <p>This session's creation date. 
     *  
	 * @return Creation timestamp.
	 *
	 * @since 0.7
	 */
	public Date getCreateDate();

	/**
     * <p>Target this session for a state. 
     *  
	 * @return An instance of the {@link StateRequest} object, which
	 *         may be further examined for more information about the outcome of this operation.  
	 *
	 * @since 0.5
	 */
	StateRequest targetForState(State state);
	
	/**
     * <p>The server connection whch created this session. 
     *  
	 * @return An instance of the {@link Connection} object, which originally created this object
	 *         via {@link Connection#getSession(Object...)}.
	 *
	 * @since 0.7
	 */	
	public Connection getConnection();
	
	/**
	 * Externally supplied configuration. A shortcut for {@code getConnection().getClient().getConfig()}
	 * See https://github.com/typesafehub/config for details on Typesafe Config.
	 * 
	 * @return An instance of the {@link Config} type.
	 * 
	 * @since 0.7
	 */
	public Config getConfig();

	/**
     * <p>Session timeout interval, as set by the server. This session will be destroyed after it is inactive
     *    for this many milliseconds. 
     *  
	 * @return Timeout interval in milliseconds.
	 *
	 * @since 0.7
	 */	
	public long getTimeoutMillis();

	/**
	 * <p> The collection of states, traversed by this session so far, and their respective visit counts. 
	 *     For each state S, the visit count in incremented by one whenever all of the following conditions are met: 
     * <ul> 
     * <li>The session is targeted for the state S</li>
     * <li>There exists a test T, which a) is instrumented on state S, b) is not OFF, and c) this session qualified for.</li>
     * </ul>

	 * 
	 * @return A map, whose entries are keyed by {@link State} and values are Integer visit counts in
	 *         that state.
	 */
	public Map<State, Integer> getTraversedStates(); 

	/**
	 * <p> The set of tests traversed by this session so far. A test T is traversed by
	 * a session when the session is targeted for a state, which a) is instrumented by T,
	 * b) T is not OFF, and c) this session qualified for T.
	 * 
	 * @return A set of object of type {@link Test}.
	 */
	public Set<Test> getTraversedTests(); 
	
	/**
	 * <p>The set of tests that this session is disqualified for. Whenever a session is disqualified
	 * for a test, it remains disqualified for that test for the life of the session even if the condition 
	 * that disqualified it may no longer hold.
	 * 
	 * @return A set of {@link Test}s which this session is disqualified for. 
	 */
	public Set<Test> getDisqualifiedTests();
		
	/**
	 * <p>The most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantCoreStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 */
	public StateRequest getStateRequest();

	/**
	 * Trigger a custom event.
	 * 
	 * @param An implementation of {@link VariantEvent}, which represents the custom event to be triggered.
	 * @since 0.7
	 */
	public void triggerEvent(VariantEvent event);
		
	/**
	 * <p>Indicates whether this session has expired. A session is expired by the server
	 * either after it has
	 * been inactive for the period of time configured by the {@code session.timeout}
	 * config key, or after the underlying {@link Connection} is closed.
	 * 
	 * @return true if this session has expired or false otherwise.
	 * @since 0.6
	 */
	public boolean isExpired();
	
	/**
	 * <p>Set a session-scoped attribute.
	 * 
	 * @name Attribute name. Cannot be <code>null</code>.
	 * @name Attribute value. Cannot be <code>null</code>.
	 * @return The string previously associated with this attribute, or <code>null</code> if none.
	 * @since 0.6
	 */
	public String setAttribute(String name, String value);
	
	/**
	 * <p>Retrieve the session-scoped attribute.
	 * 
	 * @param name Attribute name.
	 * @return The string associated with this attribute.
	 * @since 0.6
	 */
	public String getAttribute(String name);

	/**
	 * <p>Remove a session-scoped attribute.
	 * 
	 * @param name Attribute name.
	 * @return The string, previously associated with this attribute, or <code>null</code> if none.
	 * @since 0.7
	 */
	public String clearAttribute(String name);
}
