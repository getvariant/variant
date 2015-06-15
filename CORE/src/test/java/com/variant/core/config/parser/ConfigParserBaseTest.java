package com.variant.core.config.parser;

import java.util.function.Consumer;

/**
 * Common utility methods for all JUnit tests.
 */

public class ConfigParserBaseTest {
	
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
