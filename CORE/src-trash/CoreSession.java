package com.variant.core;

import java.util.Map;
import java.util.Set;

import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.CoreStateRequest;

/**
 * <p>Variant Core Session. CLEANUP
 *  
 * @author Igor Urisman
 * @since 0.5
 */
public interface CoreSession {

	/**
	 * <p>Get this session's ID.
	 * 
	 * @return Session ID.
	 * @since 0.5
	 */
	public String getId();
	
	/**
	 * <p>Get this session's creation timestamp.
	 * 
	 * @return Milliseconds since the Epoch.
	 * @since 0.6
	 */
	public long creationTimestamp();

	/**
	 * <p>The experiment schema in effect at the time this session was created. 
	 * 
	 * @return An object of type {@link Schema}.
	 * @since 0.7
	 */
	public Schema getSchema();
	
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
	 */
	public CoreStateRequest getStateRequest();
}

