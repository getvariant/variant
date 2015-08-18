package com.variant.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class VariantIoUtils {

	/**
	 * 
	 * @param resourceName
	 * @return
	 */
	public static InputStream openResourceAsStream(String resourceName) {

		InputStream result = VariantIoUtils.class.getResourceAsStream(resourceName);
		
		if (result == null) {
			throw new RuntimeException("Classpath resource by the name [" + resourceName + "] does not exist.");
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
