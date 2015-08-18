package com.variant.core.impl;

import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ViewImpl;
import com.variant.core.session.VariantSessionImpl;

/**
 * 
 * @author Igor
 *
 */
public class VariantViewRequestImpl implements VariantViewRequest {

	private VariantSessionImpl session;
	private View view;
	private String resolvedPath;
	private ViewServeEvent event;
	private boolean committed = false;
	
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
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
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
	public ViewServeEvent getViewServeEvent() {
		return event;
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
}
