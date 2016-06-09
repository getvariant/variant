package com.variant.core.impl;

import com.variant.core.util.Tuples.Pair;

public class VariantPropertiesTestFacade {

	CoreProperties props;
	
	public VariantPropertiesTestFacade(CoreProperties props) {
		this.props = (CoreProperties) props;
	}
	
	public Pair<String, String> getString(String key) {
		return props.getString(key);
	}
}
