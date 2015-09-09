package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ViewImpl;
import com.variant.core.session.TargetingPersister;
import com.variant.core.session.VariantSessionImpl;

/**
 * 
 * @author Igor
 *
 */
public class VariantViewRequestImpl implements VariantViewRequest {

	private VariantSessionImpl session;
	private View view;
	private Status status = Status.OK;
	private String resolvedPath;
	private ViewServeEvent event;
	private boolean committed = false;
	private TargetingPersister targetingPersister = null;
	
	/**
	 * 
	 * @param session
	 */
	VariantViewRequestImpl(VariantSessionImpl session, ViewImpl view) {
		this.session = session;
		this.view = view;
	}

	void setViewServeEvent(ViewServeEvent event) {
		this.event = event;
	}
	
	void setTargetingPersister(TargetingPersister targetingPersister) {
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
	public View getView() {
		return view;
	}

	@Override
	public String resolvedViewPath() {
		return resolvedPath;
	}

	@Override
	public boolean isForwarding() {
		return ! resolvedPath.equalsIgnoreCase(view.getPath());
	}
	
	@Override
	public ViewServeEvent getViewServeEvent() {
		return event;
	}


	@Override
	public TargetingPersister getTargetingPersister() {
		return targetingPersister;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public Collection<Experience> getTargetedExperiences() {
			
		ArrayList<Experience> result = new ArrayList<Experience>();
		for (Test test: view.getInstrumentedTests()) {
			if (!test.isOn()) continue;
			Experience e = targetingPersister.get(test);
			if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found.");
			result.add(e);
		}
		return result;
	}

	@Override
	public Experience getTargetedExperience(Test test) {
		
		for (Test t: view.getInstrumentedTests()) {
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
	public void setResolvedPath(String path) {
		this.resolvedPath = path;
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
