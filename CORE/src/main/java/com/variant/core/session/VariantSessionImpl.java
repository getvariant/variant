package com.variant.core.session;

import java.io.Serializable;

import com.variant.core.VariantSession;

/**
 * 
 * @author Igor
 *
 */
class VariantSessionImpl implements VariantSession, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	/**
	 * 
	 * @param id
	 */
	VariantSessionImpl(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 */
	@Override
	public String getId() {
		return id;
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
