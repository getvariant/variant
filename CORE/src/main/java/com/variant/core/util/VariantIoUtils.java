package com.variant.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class VariantIoUtils {

	/**
	 * Open one at a time, until found.
	 * @param resourceName
	 * @return
	 */
	public static InputStream openResourceAsStream(String... resourceNames) {

		
		InputStream result = null;
		
		for (String name: resourceNames) {
			result = VariantIoUtils.class.getResourceAsStream(name);
			if (result != null) break;
		}
		
		if (result == null) {
			throw new RuntimeException("Classpath resource by the name [" + VariantStringUtils.toString(resourceNames, ",") + "] does not exist.");
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static InputStream openFileAsStream(String fileName) {
		try {
			return new FileInputStream(new File(fileName));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException("File [" + fileName + "] does not exist.", e);
		}
	}

}
