package com.variant.client.impl;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.core.impl.CorePropertiesImpl;
import com.variant.core.impl.VariantCoreInitParamsImpl;

public class VariantInitParamsImpl extends VariantCoreInitParamsImpl implements VariantInitParams {

	private static final long serialVersionUID = 1L;
	private VariantClientImpl client;

	VariantInitParamsImpl(VariantClientImpl client, CorePropertiesImpl.Key key) {
		super(client.getCoreApi(), CorePropertiesImpl.Key.SESSION_STORE_CLASS_INIT);
		this.client = client;
	}

	@Override
	public VariantClient getVariantClient() {
		return client;
	}

}
