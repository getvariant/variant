package com.variant.core.impl;

import com.variant.core.VariantCoreProperties.Key;
import com.variant.core.util.Tuples.Pair;

public class VariantPropertiesTestFacade {

	CorePropertiesImpl props;
	
	public VariantPropertiesTestFacade(CorePropertiesImpl props) {
		this.props = (CorePropertiesImpl) props;
	}
	
	public Pair<String, String> getString(String propName) {
		
		for (Key key: Key.keySet()) 
			if (key.propertyName().equals(propName))
				return props.getString(key);

		return null;
	}
}
