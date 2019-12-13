package com.variant.client;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;

/**
 * Variant user session. Provides a way to maintain user state across multiple state requests. 
 * Variant sessions are managed by Variant Server and are completely distinct from those 
 * that may be managed by the host application. However, Variant server relies on the host
 * application to provide session ID tracking via an implementation of the {@link SessionIdTracker}
 * interface. Variant sessions are discarded by Variant server 
 * after a configurable session timeout period of inactivity. Once a session has expired, 
 * most methods of this class will throw a {@link SessionExpiredException}.
 *
 * @author Igor Urisman
 * @since 0.5
 *
 */
public interface Session {

	/**
     * <p>This session's unique identifier. Generated as a random 128-bit number converted to the hexadecimal notation.
   	 *
	 * @since 0.7
	 */
	String getId();

	/**
     * <p>This session's creation time stamp. 
     *  
	 * @since 0.7
	 */
	public Instant getTimestamp();

	/**
     * <p>The connection object, which originally created this session.
     *  
	 * @return An object of type {@link Connection}. Cannot be null.
	 *
	 * @since 0.7
	 */	
	public Connection getConnection();

	/**
	 * The variation schema, associated with this session.  This is the schema that was live at the time when this session was created.
	 * May not be the current live generation of this schema.
	 * 
	 * @return An object of type {@link Schema}. Cannot be null.
	 * 
	 * @since 0.9
	 */
	Schema getSchema();

	/**
     * <p>Target this session for a given state. 
     *  
	 * @return An object of type {@link StateRequest}, which
	 *         may be further examined for more information about the outcome of this operation.
	 *         Cannot be <code>null</code>
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.5
	 */
	StateRequest targetForState(State state);
		
	/**
     * <p>Session timeout interval, as set by the server. The server will dispose of this session after this many milliseconds of inactivity.
     *  
	 * @since 0.7
	 */	
	public long getTimeoutMillis();

	/**
	 * <p> The collection of states, traversed by this session so far, and their respective visit counts. 
	 *     For each state S, the visit count is incremented by one whenever the session is targeted for the state S, 
	 *     and there exists a variation V, which a) is instrumented on state S, b) is online, and c) this session qualified for.
	 * 
	 * @return A map, whose entries are keyed by {@link State} and values are the Integer visit counts of
	 *         that state. Cannot be null, but may be empty.
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
	 * @return A set of object of type {@link Variation}. Cannot be null, but may be empty.
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
	 * @return A set of objects of type {@link Variation}. Cannot be null, but may be empty.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	public Set<Variation> getDisqualifiedVariations();
		
	/**
	 * <p> The set of live experiences to which this session has been targeted.
	 * This list is different from he similarly named {@link StateRequest#getLiveExperiences()} in that
	 * it is cumulative of all the state requests performed by this session. 
	 * 
	 * @return A set of object of type {@link Experience}. Cannot be null, but may be empty.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.9
	 */
	public Set<Experience> getLiveExperiences(); 

	/**
	 * <p> The live experience in a given variation. 
	 * 
	 * @return An {@link Optional} containing the requested experience if the {@link Set}, 
	 * returned by {@link #getLiveExperiences()}, contains one for the given variation, or empty otherwise.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.9
	 */
	public Optional<Experience> getLiveExperience(Variation variation); 

	/**
	 * <p> The live experience in a given variation, by variation name.  
	 * 
	 * @return An {@link Optional} containing the requested experience if the {@link Set}, 
	 * returned by {@link #getLiveExperiences()}, contains one for the given variation, or empty otherwise.
	 * one from the given variation.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.9
	 */
	public Optional<Experience> getLiveExperience(String variationName); 

	/**
	 * <p>The most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An {@link Optional}, containing the most recent state request,
	 *         or empty if this session has not yet been targeted for a state.
	 *  
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.5
	 */
	public Optional<? extends StateRequest> getStateRequest();

	/**
	 * Trigger a custom trace event.
	 * 
	 * @param event An implementation of {@link TraceEvent}, to be triggered.
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	public void triggerTraceEvent(TraceEvent event);
		
	/**
	 * This session's attributes. 
	 * 
	 * @return An object of type {@code SessionAttributeMap}.  
	 * 
	 * @throws SessionExpiredException
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.6
	 */
	public SessionAttributes getAttributes();
		
}
