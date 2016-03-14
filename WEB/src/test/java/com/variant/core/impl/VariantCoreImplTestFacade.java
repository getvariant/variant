package com.variant.core.impl;

import com.variant.core.Variant;
import com.variant.core.session.SessionService;

public class VariantCoreImplTestFacade {
	
	private VariantCoreImpl coreImpl;
	
	public VariantCoreImplTestFacade(Variant api) {
		this.coreImpl = ((VariantCoreImpl)api);
	}
	
	/**
	 * 
	 * @return
	 */
	public VariantRuntime getRuntime() {
		return coreImpl.getRuntime();
	}
	
	/**
	 * 
	 * @return
	 */
	public SessionService getSessionService() {
		return coreImpl.getSessionService();
	}

}
