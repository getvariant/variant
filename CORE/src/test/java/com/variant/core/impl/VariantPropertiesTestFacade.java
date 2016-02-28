package com.variant.core.impl;

import com.variant.core.VariantProperties;

public class VariantPropertiesTestFacade {

	VariantPropertiesImpl props;
	
	public VariantPropertiesTestFacade(VariantProperties props) {
		this.props = (VariantPropertiesImpl) props;
	}
	
	public String getString(String key) {
		return props.getString(key);
	}
}
