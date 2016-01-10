package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import com.variant.core.VariantTargetingTracker;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.StateServeEvent;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.session.VariantSessionImpl;

/**
 * 
 * @author Igor
 *
 */
public class VariantStateRequestImpl implements VariantStateRequest {

	private VariantSessionImpl session;
	private State state;
	private Status status = Status.OK;
	private Map<String,String> resolvedParameterMap;
	private HashSet<VariantEvent> events = new HashSet<VariantEvent>();
	private boolean committed = false;
	private VariantTargetingTracker targetingPersister = null;
	private HashSet<Test> disqualifiedTests = new HashSet<Test>();
	
	/**
	 * 
	 * @param session
	 */
	VariantStateRequestImpl(VariantSessionImpl session, StateImpl state) {
		this.session = session;
		this.state = state;
		session.setStateRequest(this);
	}

	void setViewServeEvent(StateServeEvent event) {
		events.add(event);
	}
	
	void setTargetingPersister(VariantTargetingTracker targetingPersister) {
		this.targetingPersister = targetingPersister;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public VariantSession getSession() {
		return session;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Map<String,String> getResolvedParameterMap() {
		return resolvedParameterMap;
	}

	@Override
	public Collection<Test> getDisqualifiedTests() {
		return disqualifiedTests;
	}

	@Override
	public VariantTargetingTracker getTargetingTracker() {
		return targetingPersister;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public Collection<VariantEvent> getPendingEvents() {
		return Collections.unmodifiableSet(events);
	}

	@Override
	public Collection<Experience> getTargetedExperiences() {
			
		ArrayList<Experience> result = new ArrayList<Experience>();
		for (Test test: state.getInstrumentedTests()) {
			if (!test.isOn() || disqualifiedTests.contains(test)) continue;
			Experience e = targetingPersister.get(test);
			if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found.");
			result.add(e);
		}
		return result;
	}

	@Override
	public Experience getTargetedExperience(Test test) {
		
		for (Test t: state.getInstrumentedTests()) {
			if (!t.isOn() || !t.equals(test)) continue;
			Experience e = targetingPersister.get(test);
			if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found.");
			return e;
		}		
		return null;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param path
	 */
	public void setResolvedParameters(Map<String,String> parameterMap) {
		this.resolvedParameterMap = parameterMap;
	}

	/**
	 * 
	 */
	public void commit() {
		committed = true;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isCommitted() {
		return committed;
	}
	
	/**
	 * 
	 * @return
	 *
	public StateServeEvent getStateServeEvent() {
		return event;
	}
*/
	
	/**
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * 
	 * @param test
	 */
	public void addDisqualifiedTest(Test test) {
		disqualifiedTests.add(test);
	}

}
