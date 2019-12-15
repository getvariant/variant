package com.variant.server.api;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.Variation;

/**
 * User session.
 *
 * @author Igor Urisman
 * @since 0.7
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
     * <p>This session's creation timestamp. 
     *  
	 * @return Creation timestamp.
	 *
	 * @since 0.7
	 */
	public Instant getTimestamp();

	/**
	 * <p> The collection of states, traversed by this session so far, and their respective visit counts. 
	 *     For each state S, the visit count in incremented by one whenever all of the following conditions are met: 
     * <ul> 
     * <li>The session is targeted for the state S</li>
     * <li>There exists a variation V, which a) is instrumented on state S, b) is not OFF, and c) this session qualified for.</li>
     * </ul>

	 * 
	 * @return A map, whose entries are keyed by {@link State} and values are Integer visit counts in
	 *         that state.
	 *         
    * @since 0.7
	 */
	public Map<State, Integer> getTraversedStates(); 

	/**
	 * The variation schema, associated with this session.
	 *  
	 * @return An object of type {@link Schema}
	 * 
	 * @since 0.9
	 */
	public Schema getSchema();

	/**
	 * Variant server's current runtime configuration.
	 *  
	 * @return An object of type {@link Configuration}. Cannot be null.
	 * 
	 * @since 0.9
	 */
	public Configuration getConfiguration();

	/**
	 * <p> The set of variations traversed by this session so far. A variation V 
	 * is traversed by a session when the session is targeted for a state, 
	 * which a) is instrumented by V, b) V is not OFF, and c) this session is
	 * qualified for V.
	 * 
	 * @return A set of {@link Variation} objects.
	 * 
    * @since 0.7
	 */
	public Set<Variation> getTraversedVariations(); 
	
	/**
	 * <p>The set of variations, which this session is disqualified for. Whenever a session is disqualified
	 * for a variation, it remains disqualified for that test for the life of the session even if the condition 
	 * that disqualified it may no longer hold.
	 * 
	 * @return A set of {@link Variation}s objects. 
	 * 
    * @since 0.7
	 */
	public Set<Variation> getDisqualifiedVariations();
		
	/**
	 * <p>The most recent state request, which may be still in progress or already committed or failed.
	 * 
	 * @return An {@link Optional} of {@link StateRequest}, containing the most recent state request,
	 *         or empty if this session has not yet been targeted for a state.
	 *  
	 * @since 0.7
	 */
	public Optional<StateRequest> getStateRequest();

	/**
	 * <p>This session's attributes as a mutable map.
	 * 
	 * @return A mutable {@link Map}.
	 * @since 0.7
	 */
	public Map<String, String> getAttributes();	
}

