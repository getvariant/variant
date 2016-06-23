package com.variant.core.impl;

import com.variant.core.session.CoreSessionService;

public class VariantCoreTestFacade {
	
	/**
	 * 
	 * @return
	 *
	public VariantRuntime getRuntime() {
		return coreImpl.getRuntime();
	}
	*/
	/**
	 * 
	 * @return
	 */
	public static CoreSessionService getSessionService(VariantCore core) {
		return core.getSessionService();
	}

}
