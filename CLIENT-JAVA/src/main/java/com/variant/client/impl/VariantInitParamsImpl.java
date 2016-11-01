package com.variant.client.impl;

import com.variant.client.SystemProperties.Property;
import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.core.impl.VariantCoreInitParamsImpl;

public class VariantInitParamsImpl extends VariantCoreInitParamsImpl implements VariantInitParams {

	private static final long serialVersionUID = 1L;
	private VariantClientImpl client;

	/**
	 * 
	 * @param client
	 * @param key
	 */
	public VariantInitParamsImpl(VariantClientImpl client, Property prop) throws Exception {
		super(client.getCoreApi(), client.getProperties().get(prop).toString());
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
