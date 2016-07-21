package com.variant.client.impl;

import com.variant.client.VariantClient;
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
	
}
