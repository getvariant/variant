package com.variant.server.api;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * The server side of a Variant user session, contains session-scoped application 
 * state.
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
     * <p>This session's creation date. 
     *  
	 * @return Creation timestamp.
	 *
	 * @since 0.7
	 */
	public Date getCreateDate();

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
	 *         
    * @since 0.7
	 */
	public Map<State, Integer> getTraversedStates(); 

	/**
	 * Get variation schema, associated with this session.
	 *  
	 * @return An object of type {@link Schema}
	 * 
	 * @since 0.9
	 */
	public Schema getSchema();

	/**
	 * <p> The set of tests traversed by this session so far. A test T is traversed by
	 * a session when the session is targeted for a state, which a) is instrumented by T,
	 * b) T is not OFF, and c) this session qualified for T.
	 * 
	 * @return A set of object of type {@link Test}.
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
    * @since 0.7
	 */
	public Set<Test> getDisqualifiedTests();
		
	/**
	 * <p>The most recent state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantCoreStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.7
	 */
	public StateRequest getStateRequest();

	/**
	 * <p>Set the value of a session attribute.
	 * 
	 * @return The string value, previously associated with this attribute, or <code>null</code> if none.
	 * @since 0.7
	 */
	public String setAttribute(String name, String value);
	
	/**
	 * Retrieve a session attribute.
	 * 
	 * @return The string value, associated with this attribute, or <code>null</code> if none.
	 * @since 0.7
	 */
	public String getAttribute(String name);

	/**
	 * Remove a session attribute.
	 * 
	 * @return The string value, previously associated with this attribute, or <code>null</code> if none.
	 * @since 0.7
	 */
	public String clearAttribute(String name);
}

