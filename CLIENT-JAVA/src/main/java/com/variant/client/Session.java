package com.variant.client;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.typesafe.config.Config;
import com.variant.client.lifecycle.LifecycleEvent;
import com.variant.client.lifecycle.LifecycleHook;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * Represents a Variant user session. Provides a way to 
 * identify a user across multiple state requests and contains session-scoped application 
 * state that must be preserved between state requests. 
 * <p>
 * Variant server provides distributed
 * session management which does not rely on the host application's own notion of the user
 * session. This way host sessions and Variant sessions remain decoupled, leaving the host
 * application free to recreate its session without affecting experience variations
 * managed by Variant. For example, a Web application natively relies on the HTTP session, 
 * provided to it by a Web container, like Apache Tomcat. 
 * If a Variant experience variation starts on a public page and continues past the login page, 
 * Variant sessions will not be disrupted should the application re-create its native session
 * at login.
 *    
 * <p> Variant sessions are expired by Variant server after a configurable session timeout 
 *     period of inactivity. Once a session has expired, most methods of this class will throw 
 *     a {@link SessionExpiredException}.
 *
 * @author Igor Urisman
 * @since 0.5
 *
 */
public interface Session {

	/**
     * <p>This session's unique identifier.
     *  
	 * @return This session's unique identifier.  
	 *
	 * @since 0.7
	 */
	String getId();

	/**
     * <p>This session's creation date. 
     *  
	 * @return This session's creation date.
	 *
	 * @since 0.7
	 */
	public Date getCreateDate();

	/**
     * <p>The connection object, which originally created this session via {@link Connection#getSession(Object...)}.
     *  
	 * @return An object of type {@link Connection}.
	 *
	 * @since 0.7
	 */	
	public Connection getConnection();

	/**
     * <p>Target this session for a state. 
     *  
	 * @return An object of type {@link StateRequest}, which
	 *         may be further examined for more information about the outcome of this operation.  
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * @throws StateNotInstrumentedException 
	 * 
	 * @since 0.5
	 */
	StateRequest targetForState(State state);
		
	/**
	 * Externally supplied configuration. A shortcut for {@code getConnection().getClient().getConfig()}
	 * See Variant Java Client User Guile for details on configuring Variant Java client.
	 * 
	 * @return An object of type <a href="https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html" target="_blank">com.typesafe.config.Config</a>.
	 * 
	 * @since 0.7
	 */
	public Config getConfig();

	/**
     * <p>Session timeout interval, as set by the server. The server will dispose of this session after this many milliseconds of inactivity.
     *  
	 * @return Timeout interval in milliseconds.
	 *
	 * @see #isExpired()
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
	 * @return A map, whose entries are keyed by {@link State} and values are Integer visit counts of
	 *         that state.
	 *         
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	public Map<State, Integer> getTraversedStates(); 

	/**
	 * <p> The set of tests traversed by this session so far. A test T is traversed by
	 * a session when the session is targeted for a state instrumented by T, T is not OFF, 
	 * and the session is qualified for T.
	 * 
	 * @return A set of object of type {@link Test}.
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	public Set<Test> getTraversedTests(); 
	
	/**
	 * <p>The set of tests that this session is disqualified for. Whenever a session is disqualified
	 * for a test, it remains disqualified for that test for the life of the session even if the condition 
	 * that disqualified it may no longer hold.
	 * 
	 * @return A set of {@link Test}s which this session is disqualified for. 
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	public Set<Test> getDisqualifiedTests();
		
	/**
	 * <p>The most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantCoreStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.5
	 */
	public StateRequest getStateRequest();

	/**
	 * Trigger a custom event.
	 * 
	 * @param event An implementation of {@link VariantEvent}, which represents the custom event to be triggered.
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	public void triggerEvent(VariantEvent event);
		
	/**
	 * <p>Indicates whether this session has expired. A session is expired by the server
	 * after a configurable period of inactivity.
	 * 
	 * @return true if this session has expired or false otherwise. 
     *
	 * @see #getTimeoutMillis()
	 * @since 0.6
	 */
	public boolean isExpired();
	
	/**
	 * <p>Set a session attribute.
	 * 
	 * @param name Attribute name. Cannot be <code>null</code>.
	 * @param value Attribute value. Cannot be <code>null</code>.
	 * @return The string value previously associated with this <code>name</code>, or <code>null</code> if none.
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.6
	 */
	public String setAttribute(String name, String value);
	
	/**
	 * <p>Retrieve the value of a session attribute.
	 * 
	 * @param name Attribute name.
	 * @return The string value associated with this name.
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.6
	 */
	public String getAttribute(String name);

	/**
	 * <p>Remove a session attribute.
	 * 
	 * @param name Attribute name.
	 * @return The string value previously associated with this <code>name</name>, or <code>null</code> if none.
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	public String clearAttribute(String name);
	
	/**
	 * Register a session-level life-cycle hook. Hooks are posted asynchronously.
	 * If multiple hooks are registered for a particular life-cycle event, connection-level
	 * hooks are posted before session-level hooks.
	 * 
	 * @param hook An implementation of {@link LifecycleHook}
	 * 
	 * @throws SessionExpiredException
	 * @throws ConnectionClosedException
	 *
	 * @see Connection#addLifecycleHook(LifecycleHook)
	 * @since 0.9
	 */
	void addLifecycleHook(LifecycleHook<? extends LifecycleEvent> hook);

}
