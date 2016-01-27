package com.variant.core.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections4.Predicate;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.VariantEventDecorator;
import com.variant.core.event.impl.StateVisitedEvent;
import com.variant.core.event.impl.VariantEventDecoratorImpl;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.session.VariantSessionImpl;

/**
 * 
 * @author Igor
 *
 */
public class VariantStateRequestImpl implements VariantStateRequest, Serializable {

	/**
	 * Needs serializable because we keep it in session.
	 */
	private static final long serialVersionUID = 1L;
	
	private VariantSessionImpl session;	
	private State state;
	private Status status = Status.OK;
	private Map<String,String> resolvedParameterMap;
	private HashSet<VariantEventDecorator> events = new HashSet<VariantEventDecorator>();
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
		
		// State visited event gets status from this request
		for (VariantEvent event: getPendingEvents(
				new Predicate<VariantEvent>() {
					
					@Override
					public boolean evaluate(VariantEvent e) {
						VariantEvent origEvent = ((VariantEventDecoratorImpl)e).getOriginalEvent();
						return origEvent instanceof StateVisitedEvent;
					}
				})
			) {
			
			// The status of this request.
			event.getParameterMap().put("REQ_STATUS", status);

			// log all resolved state params as event params.
			for (Map.Entry<String,String> e: resolvedParameterMap.entrySet()) {
				event.getParameterMap().put(e.getKey(), e.getValue());				
			}
		}

		((VariantCoreImpl) Variant.Factory.getInstance()).getEventWriter().write(events);	
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
		if (event == null)
			throw new IllegalArgumentException("Event parameter cannot be null");
		
		events.add(new VariantEventDecoratorImpl(event, this));
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
