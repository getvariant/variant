package com.variant.core;

import com.variant.core.session.TargetingPersister;

public interface VariantSession {

	/**
	 * This session ID.
	 * @return
	 */
	public String getId();
	
	/**
	 * Initialize the targeting persister. Targeting persister has the shortest life time and needs to be re-created
	 * at the beginning of each view request.
	 * 
	 * @param userData
	 */
	public void initTargetingPersister(Object userData) throws VariantBootstrapException;
	
	public TargetingPersister getTargetingPersister();

}
