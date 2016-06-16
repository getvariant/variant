package com.variant.client.impl;

import com.variant.client.VariantClient;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.impl.VariantCore;

public class VariantClientTestFacade {

	public static VariantCore getCoreApi(VariantClient client) {	
		return ((VariantClientImpl)client).getCoreApi();
	}
}
