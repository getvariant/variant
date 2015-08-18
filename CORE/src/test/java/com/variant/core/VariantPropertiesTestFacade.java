package com.variant.core;

public class VariantPropertiesTestFacade {
	
	public static String getString(String key) {
		return VariantProperties.getInstance().getString(key);
	}
}
