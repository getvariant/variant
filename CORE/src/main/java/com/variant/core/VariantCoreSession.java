package com.variant.core;

import java.util.Collection;

import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 * <p>Variant Core Session. CLEANUP
 *  
 * @author Igor Urisman
 * @since 0.5
 *
public interface VariantCoreSession {

	/**
	 * <p>Get this session's ID.
	 * 
	 * @return Session ID.
	 * @since 0.5
	 *
	public String getId();
	
	/**
	 * <p>Get this session's creation timestamp.
	 * 
	 * @return Milliseconds since the Epoch.
	 * @since 0.6
	 *
	public long creationTimestamp();

	/**
	 * <p>The experiment schema in effect at the time this session was created. 
	 * 
	 * @return An object of type {@link Schema}.
	 * @since 0.7
	 *
	public Schema getSchema();
	
	/**
	 * <p> The collection of states traversed by this session so far and their counts. For each state S,
	 * the traversal count in incremented by one whenever all of the following conditions are met: 
     * <ul> 
     * <li>The session is targeted for the state S</li>
     * <li>There exists a test T, which a) instruments state S, b) is not OFF, and c) this session qualified for.</li>
     * </ul>

	 * 
	 * @return A collection of {@link Pair}s of ({@link State}, Integer), corresponding
	 *         to the traversed states. Call {@link Pair#arg1()} to obtain the state and 
	 *         {@link Pair#arg2()} to obtain the count of times this state has been traversed.
	 *
	public Collection<Pair<State, Integer>> getTraversedStates(); 

	/**
	 * <p> The collection of tests traversed by this session so far. A test T is traversed by
	 * a session when the session is targeted for a state, which a) is instrumented by T,
	 * b) T is not OFF, and c) this session qualified for T.
	 * 
	 * @return A collection of object of type {@link Test}.
	 *
	public Collection<Test> getTraversedTests(); 
	
	/**
	 * <p>The collection of tests that this session is disqualified for. Whenever a session is disqualified
	 * for a test, it remains disqualified for that test for the life of the session even if the condition 
	 * that disqualified it may no longer hold.
	 * 
	 * @return A collection of {@link Test}s which this session is disqualified for. 
	 *
	public Collection<Test> getDisqualifiedTests();
		
	/* ON SERVER NOW
	 * Trigger a custom event.
	 * 
	 * @param An implementation of {@link VariantEvent} which represents the custom event to be triggered.
	 * @since 0.5
	 *
	public void triggerEvent(VariantEvent event);
    */
	/**
	 * <p>Get most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantCoreStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 *
	public VariantCoreStateRequest getStateRequest();
}
*/
