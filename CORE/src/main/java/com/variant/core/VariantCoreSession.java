package com.variant.core;

import java.util.Collection;

import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 * <p>Represents a Variant user session. Variant has its own notion of user session, 
 *    independent from that of the host application. Variant session provides a way to 
 *    identify the user across multiple state requests and contains session-scoped application 
 *    state that must be preserved between state requests. Variant server acts as the session 
 *    store by maintaining a map of user session objects keyed by session ID.
 *    Variant maintains its own session, rather than relying on the host application’s, 
 *    because it is frequently desirable for Variant session to survive the destruction 
 *    of the host application’s session.
 * </p>
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantCoreSession {

	/**
	 * <p>Get this session's ID.
	 * 
	 * @return Session ID as a 128 bit binary number converted to hexadecimal representation.
	 * @since 0.5
	 */
	public String getId();
	
	/**
	 * <p>Get this session's creation timestamp millis.
	 * 
	 * @return Milliseconds since the Epoch.
	 * @since 0.6
	 */	
	public long creationTimestamp();

	/**
	 * <p>The ID of the schema in effect at the time this session was created. Any subsequent operations
	 * on this session will fail if this schema is replaced with another schema. 
	 * 
	 * @return Milliseconds since the Epoch.
	 * @since 0.6
	 */	
	public String getSchemaId();
	
	/**
	 * <p> The collection of states traversed by this session so far and their counts. For each state S,
	 * the traversal count in incremented by one whenever all of the following conditions are met: 
     * <ul> 
     * <li>The session is targeted for the state S</li>
     * <li>There exists a test T, which a) instruments state S, b) is not OFF, and c) this session qualified for.</li>
     * </ul>

	 * 
	 * @return A collection of {@link Pair}s of type <{@link State}, Integer> corresponding
	 *         to the traversed states. Use <code>arg1()</code> to obtain the state and 
	 *         <code>arg2()</code> to obtain the count of times this state has been traversed.
	 */
	public Collection<Pair<State, Integer>> getTraversedStates(); 

	/**
     * <p>Target session for a state. 
     *  
	 * @return An instance of the {@link com.variant.core.VariantCoreStateRequest} object, which
	 *         may be further examined for more information about targeting.  
	 *
	 * @since 0.5
	 */
	public VariantCoreStateRequest targetForState(State state);

	/**
	 * <p> The collection of tests traversed by this session so far. A test T is traversed by
	 * a session when the session is targeted for a state, which a) is instrumented by T,
	 * b) T is not OFF, and c) this session qualified for T.
	 * 
	 * @return A collection of object of type {@link Test}.
	 */
	public Collection<Test> getTraversedTests(); 
	
	/**
	 * <p>The collection of tests that this session has been disqualified for.
	 * 
	 * @return A collection of {@link Test}s which this session has been disqualified for. 
	 */
	public Collection<Test> getDisqualifiedTests();
		
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
	 * @return An object of type {@link VariantCoreStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 */
	public VariantCoreStateRequest getStateRequest();
}
