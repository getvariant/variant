package com.variant.core.tests;

import java.io.InputStream;
import java.util.Date;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.BeforeClass;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantTestFacade;
import com.variant.core.conf.ApplicationProperties;
import com.variant.core.error.ParserError;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.TargetingPersisterFromString.UserDataFromString;
import com.variant.core.util.VariantJunitLogger;

/**
 * Common utility methods for all JUnit tests.
 */

public class BaseTest {
	
	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeTest() throws Exception {
		
		// Replace the logger with the in-memory implementation that we can
		// introspect into.
		VariantTestFacade.setLogger(new VariantJunitLogger(System.out));
		
	}

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
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	static protected Experience experience(String name) {
		String[] tokens = name.split("\\.");
		return Variant.getSchema().getTest(tokens[0]).getExperience(tokens[1]);
	}

	/**
	 * 
	 * @param args
	 * @return
	 *
	static protected UserDataFromString targetingPersisterString(Object...args) {
		if (args.length % 2 != 0) 
			throw new IllegalArgumentException("Must pass an even number of arguments");
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i % 2 == 0) {
				// Timestamp
				if (i > 0) result.append("|");
				result.append(((Date)args[i]).getTime()).append(".");
			}
			else {
				// Experience
				result.append((Experience) args[i]);
			}
		}
		return new UserDataFromString(result.toString());
	}
	*/
}
