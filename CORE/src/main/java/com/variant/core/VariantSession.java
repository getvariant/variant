package com.variant.core;

import java.util.Collection;

import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 * <p>Represents a Variant user session.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantSession {

	/**
	 * <p>Get this session's ID.
	 * 
	 * @return Session ID as a 128 bit binary number converted to hexadecimal representation.
	 * @since 0.5
	 */
	public String getId();
	
	/**
	 * <p> The collection of states traversed by this session so far and their counts.
	 * 
	 * @return A collection of {@link Pair}s of type <{@link State}, Integer> corresponding
	 *         the traversed states. Use <code>arg1()</code> to obtain the state and 
	 *         <code>arg2()</code> to obtain the count of times this state has been traversed.
	 */
	public Collection<Pair<State, Integer>> getTraversedStates(); 

	/**
	 * <p> The collection of tests traversed by this session so far and their  qualifications. 
	 * Each time a session requests a new state, the tests instrumented on this 
	 * state but not yet traversed by this session will be re-qualified. The outcome of this 
	 * qualification will be cached for the duration of this session.
	 * 
	 * @return A collection of {@link Pair}s of type <{@link Test}, Boolean> corresponding
	 *         to traversed Tests. Use <code>arg1()</code> to obtain the test and 
	 *         <code>arg2()</code> to obtain the its qualification.
	 */
	public Collection<Pair<Test, Boolean>> getTraversedTests(); 
	
	/**
	 * Trigger a custom event.
	 * 
	 * @param The custom event to be logged. An implementation of {@link VariantEvent}
	 * @since 0.5
	 */
	public void triggerEvent(VariantEvent event);

	/**
	 * <p>Get most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 */
	public VariantStateRequest getStateRequest();
}
