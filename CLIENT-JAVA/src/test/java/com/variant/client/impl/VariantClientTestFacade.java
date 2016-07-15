package com.variant.client.impl;

import com.variant.client.VariantClient;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.session.ClientSessionService;
import com.variant.core.impl.VariantCore;

public class VariantClientTestFacade {

	/**
	 * 
	 * @param client
	 * @return
	 */
	public static VariantCore getCoreApi(VariantClient client) {	
		return ((VariantClientImpl)client).getCoreApi();
	}
	
	/**
	 * 
	 * @param client
	 * @return
	 */
	public static ClientSessionService getSessionService(VariantClient client) {	
		return ((VariantClientImpl)client).getSessionService();
	}

}
