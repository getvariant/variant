package com.variant.core.tests;

import java.util.function.Consumer;

import com.variant.core.schema.impl.ParserError;
import com.variant.core.schema.impl.ParserResponse;

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
}
