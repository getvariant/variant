package com.variant.core.session;

import java.io.Serializable;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;

/**
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl implements VariantSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private VariantStateRequest currentRequest = null;
	
	/**
	 * 
	 * @param id
	 */
	VariantSessionImpl(String id) {
		this.id = id;
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public VariantStateRequest getStateRequest() {
		return currentRequest;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param req
	 */
	public void setStateRequest(VariantStateRequest req) {
		currentRequest = req;
	}

	@Override
	public boolean equals(Object o) {
		try {
			VariantSessionImpl other = (VariantSessionImpl) o;
			return id.equals(other.id);
		}
		catch(ClassCastException e) {
			return false;
		}
	}


}
