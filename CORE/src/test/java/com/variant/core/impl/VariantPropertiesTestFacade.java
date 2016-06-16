package com.variant.core.impl;

import com.variant.core.util.Tuples.Pair;

public class VariantPropertiesTestFacade {

	CorePropertiesImpl props;
	
	public VariantPropertiesTestFacade(CorePropertiesImpl props) {
		this.props = (CorePropertiesImpl) props;
	}
	
	public Pair<String, String> getString(String key) {
		return props.getString(key);
	}
}
