package com.variant.client;

import java.util.Map;
import java.util.Set;

import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * <p>Represents a Variant user session. Variant has its own notion of user session, 
 *    independent from that of the host application's. Variant session provides a way to 
 *    identify the user across multiple state requests and contains session-scoped application 
 *    state that must be preserved between state requests. Variant server acts as the session 
 *    store by maintaining a map of user session objects keyed by session ID.
 *    Variant maintains its own session, rather than relying on the host application's, 
 *    because it is frequently desirable for Variant session to survive the destruction 
 *    of the host application's session.
 * 
 * <p> Variant session expires when either a configurable session timeout period of inactivity,
 *     or after a schema redeployment. Once the session represented by this object has expired,
 *     all subsequent operations on it, apart from {@code getId()} and {@code isExpired()} will throw 
 *     {@link VariantRuntimeUserErrorException}.
 *
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface Session {

	/**
     * <p>Session ID. 
     *  
	 * @return Session ID.  
	 *
	 * @since 0.7
	 */
	String getId();

	/**
     * <p>This session's creation timestamp as the Epoch time. 
     *  
	 * @return Creation timestamp.
	 *
	 * @since 0.7
	 */
	public long creationTimestamp();

	/**
     * <p>Target session for a state. 
     *  
	 * @return An instance of the {@link StateRequest} object, which
	 *         may be further examined for more information about the outcome of this operation.  
	 *
	 * @since 0.5
	 *
	VariantStateRequest targetForState(State state);
	
	/**
     * <p>The server connection whch created this session. 
     *  
	 * @return An instance of the {@link Connection} object, which originally created this object
	 *         via {@link Connection#getSession(Object...)}.
	 *
	 * @since 0.7
	 */	
	public Connection getConnectoin();
	
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
	 *     For each state S, the traversal count in incremented by one whenever all of the following conditions are met: 
     * <ul> 
     * <li>The session is targeted for the state S</li>
     * <li>There exists a test T, which a) instruments state S, b) is not OFF, and c) this session qualified for.</li>
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
	 * <p>Get most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantCoreStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 *
	public VariantStateRequest getStateRequest();

	/**
	 * Trigger a custom event.
	 * 
	 * @param An implementation of {@link VariantEvent} which represents the custom event to be triggered.
	 * @since 0.7
	 */
	public void triggerEvent(VariantEvent event);
		
	/**
	 * <p>Indicates whether this session has expired. A session expires either after it has
	 * been inactive for the period of time configured by the {@code session.timeout.secs}
	 * system property, or after the schema which was in effect during its creation, has 
	 * been undeployed.
	 * 
	 * @return true if this session has expired or false otherwise.
	 * @since 0.6
	 */
	public boolean isExpired();

	/**
	 * <p>Set a session-scoped attribute.
	 * 
	 * @return The object which was previously associated with this attribute, or null if none.
	 * @since 0.6
	 */
	public Object setAttribute(String name, Object value);
	
	/**
	 * <p>Retrieve a session-scoped attribute.
	 * 
	 * @return The object associated with this attribute.
	 * @since 0.6
	 */
	public Object getAttribute(String name);

}
