package com.variant.core.util;

import java.io.InputStream;

public class VariantIoUtils {

	public static InputStream openResourceAsStream(String resourceName) {

		InputStream result = VariantIoUtils.class.getResourceAsStream(resourceName);
		
		if (result == null) {
			throw new RuntimeException("Classpath resource by the name '" + resourceName + "' does not exist.");
		}
		
		return result;
	}
}
