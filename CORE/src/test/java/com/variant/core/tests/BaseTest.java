package com.variant.core.tests;

import java.io.InputStream;
import java.util.function.Consumer;

import com.variant.core.ParserResponse;
import com.variant.core.conf.ApplicationProperties;
import com.variant.core.error.ParserError;

/**
 * Common utility methods for all JUnit tests.
 */

public class BaseTest {
	
	/**
	 * Print all errors to std out.
	 * @param response
	 */
	static protected void printErrors(ParserResponse response) {
		if (response.hasErrors()) {
			response.getErrors().forEach(
					new Consumer<ParserError>() {
						public void accept(ParserError pe) {
							System.out.println(pe);
						}
					}
			);
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	static protected InputStream openResourceAsInputStream(String name) {
		InputStream result = ApplicationProperties.class.getResourceAsStream(name);
		if (result == null) {
			throw new RuntimeException("Classpath resource '" + name + "' does not exist.");
		}
		return result;
	}
}
