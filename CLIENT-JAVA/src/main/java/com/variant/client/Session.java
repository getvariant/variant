package com.variant.client;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.typesafe.config.Config;
import com.variant.core.TraceEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;

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
	 * Get variation schema, associated with this session.
	 * 
	 * @return An object of type {@link Schema}
	 * 
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.9
	 */
	Schema getSchema();

	/**
     * <p>Target this session for a state. 
     *  
	 * @return An object of type {@link StateRequest}, which
	 *         may be further examined for more information about the outcome of this operation.  
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
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
	 *     For each state S, the visit count is incremented by one whenever all of the following conditions are met: 
     * <ul> 
     * <li>The session is targeted for the state S</li>
     * <li>There exists a variation V, which a) is instrumented on state S, b) is online, and c) this session qualified for.</li>
     * </ul>

	 * 
	 * @return A map, whose entries are keyed by {@link State} and values are Integer visit counts of
	 *         that state.
	 *         
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	public Map<State, Integer> getTraversedStates(); 

	/**
	 * <p> The set of variations, traversed by this session so far. A variation V is traversed by
	 * a session when the session is targeted for a state instrumented by V, V is online, 
	 * and the session is qualified for V.
	 * 
	 * @return A set of object of type {@link Variation}.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	public Set<Variation> getTraversedVariations(); 
	
	/**
	 * <p>The set of variations for which this session has been disqualified. Whenever a session is disqualified
	 * for a variation, it remains disqualified for it for the life of the session, even if the condition 
	 * that disqualified it may no longer hold.
	 * 
	 * @return A set of objects of type {@link Variation}. 
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	public Set<Variation> getDisqualifiedVariations();
		
	/**
	 * <p>The most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An {@link Optional} of {@link StateRequest}, containing the most recent state request,
	 *         or empty if this session has not yet been targeted for a state.
	 *  
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.5
	 */
	public Optional<StateRequest> getStateRequest();

	/**
	 * Trigger a custom trace event.
	 * 
	 * @param event An implementation of {@link TraceEvent}, which represents the custom trace event to be triggered.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	public void triggerTraceEvent(TraceEvent event);
		
	/**
	 * The mutable map containing the most recent snapshot of this session's attributes. 
	 * All non-mutating operations on the returned map are local and do not result in a 
	 * remote call to Variant server. The mutating operations, e.g. {@code put(String, String)},
	 * {@code remove(String)} or {@code clear()} result in a remote call and hence are more expensive.
	 * All such calls will refresh the local state to reflect the current state attributes from the
	 * server. If you wish to get the most up-to-date attributes without making a mutating
	 * call, you should call this method again. 
	 * 
	 * @return An object of type {@code Map<String,String>}.  
	 *         
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.6
	 */
	public Map<String, String> getAttributes();
		
}
