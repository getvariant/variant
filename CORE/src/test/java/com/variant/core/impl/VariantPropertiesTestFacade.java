package com.variant.core.impl;

import com.variant.core.VariantProperties;
import com.variant.core.util.Tuples.Pair;

public class VariantPropertiesTestFacade {

	VariantPropertiesImpl props;
	
	public VariantPropertiesTestFacade(VariantProperties props) {
		this.props = (VariantPropertiesImpl) props;
	}
	
	public Pair<String, String> getString(String key) {
		return props.getString(key);
	}
}
