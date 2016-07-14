package com.variant.client.impl;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.core.VariantCorePropertyKeys;
import com.variant.core.impl.VariantCoreInitParamsImpl;

public class VariantInitParamsImpl extends VariantCoreInitParamsImpl implements VariantInitParams {

	private static final long serialVersionUID = 1L;
	private VariantClientImpl client;

	/**
	 * 
	 * @param client
	 * @param key
	 */
	public VariantInitParamsImpl(VariantClientImpl client, VariantCorePropertyKeys.Key key) {
		super(client.getCoreApi(), key);
		this.client = client;
	}

	/**
	 * 
	 */
	@Override
	public VariantClient getVariantClient() {
		return client;
	}

}
