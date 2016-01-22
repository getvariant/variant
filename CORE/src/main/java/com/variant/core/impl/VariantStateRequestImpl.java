package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.collections4.Predicate;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.session.VariantSessionImpl;
import com.variant.core.util.Tuples.Pair;

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
	private VariantTargetingTracker targetingTracker = null;
	
	/**
	 * 
	 * @param session
	 */
	VariantStateRequestImpl(VariantSessionImpl session, StateImpl state) {
		this.session = session;
		this.state = state;
		session.setStateRequest(this);
	}
	
	void setTargetingPersister(VariantTargetingTracker targetingPersister) {
		this.targetingTracker = targetingPersister;
	}
	
	// Flush pending events to an implementation of EventPersister. 
	void flushEvents() {

		// Build a collection of targeted experiences that aren't disqualified.
		Collection<Experience> eventVariants = new LinkedList<Experience>();
		for (Experience e: getTargetedExperiences()) 
			if (session.isQualified(e.getTest())) eventVariants.add(e);

		// There may not be anything to write if we hit a known state 
		// that did not have any active tests instrumented on it.
		if (eventVariants.size() > 0) {
			EventWriter ew = ((VariantCoreImpl) Variant.Factory.getInstance()).getEventWriter();
			ew.write(new Pair<Collection<VariantEvent>, Collection<Experience>>(events, eventVariants));	
		}
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
	public VariantTargetingTracker getTargetingTracker() {
		return targetingTracker;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public Collection<VariantEvent> getPendingEvents() {
		
		return getPendingEvents(
			new Predicate<VariantEvent>() {
				@Override
				public boolean evaluate(VariantEvent e) {return true;}
			}
		);
	}

	@Override
	public Collection<VariantEvent> getPendingEvents(Predicate<VariantEvent> filter) {
		
		if (filter == null) throw new IllegalArgumentException("Filter cannot be null");
		
		HashSet<VariantEvent> result = new HashSet<VariantEvent>();
		for (VariantEvent e: events) if (filter.evaluate(e)) result.add(e);
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Collection<Experience> getTargetedExperiences() {
			
		ArrayList<Experience> result = new ArrayList<Experience>();
		for (Test test: state.getInstrumentedTests()) {
			if (!test.isOn() || session.isDisqualified(test)) continue;
			Experience e = targetingTracker.get(test);
			if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found in targeting tracker.");
			result.add(e);
		}
		return result;
	}

	@Override
	public Experience getTargetedExperience(Test test) {
		
		boolean found = false;
		
		for (Test t: state.getInstrumentedTests()) {

			if (!t.equals(test)) continue;
			found = true;
			if (!t.isOn() || session.isDisqualified(test)) continue;
			
			Experience e = targetingTracker.get(test);
			if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found in targeting tracker.");
			return e;
		}
		
		if (!found) throw new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, state.getName(), test.getName());

		return null;
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		events.add(event);
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
	 */
	public Status getStatus() {
		return status;
	}

}
