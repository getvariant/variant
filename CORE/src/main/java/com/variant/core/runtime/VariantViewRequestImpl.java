package com.variant.core.runtime;

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
	
	/**
	 * 
	 * @param session
	 */
	VariantViewRequestImpl(VariantSessionImpl session, ViewImpl view) {
		this.session = session;
		this.view = view;
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

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public void setResolvedPath(String path) {
		this.resolvedPath = path;
	}

}
