package com.variant.client;

import com.variant.client.VariantClient;
import com.variant.core.impl.VariantCore;

public class VariantClientTestFacade {

	public static VariantCore getCoreApi(VariantClient client) {	
		return client.getCoreApi();
	}
}
