package com.variant.core.config;

import com.variant.core.config.VariantProperties;

public class VariantPropertiesTestFacade {
	
	public static String getString(String key) {
		return VariantProperties.getInstance().getString(key);
	}
}
