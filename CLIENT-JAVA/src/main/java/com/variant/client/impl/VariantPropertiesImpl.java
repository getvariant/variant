package com.variant.client.impl;

import com.variant.client.VariantProperties;
import com.variant.core.impl.CorePropertiesImpl;

/**
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantPropertiesImpl implements VariantProperties {

	CorePropertiesImpl coreProperties;
	
	VariantPropertiesImpl(CorePropertiesImpl coreProperties) {
		this.coreProperties = coreProperties;
	}

	@Override
	public <T> T get(Key key, Class<T> clazz) {
		return coreProperties.get(key, clazz);
	}
	
}
