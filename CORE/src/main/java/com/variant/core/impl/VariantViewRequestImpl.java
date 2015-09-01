package com.variant.core.impl;

import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
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
