package com.variant.core.tests;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.VariantTestFacade;
import com.variant.core.error.ParserError;
import com.variant.core.jdbc.EventPersisterJdbc;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.schema.Test.Experience;
import com.variant.core.util.PropertiesChain;
import com.variant.core.util.VariantIoUtils;
import com.variant.core.util.VariantJunitLogger;

/**
 * Common utility methods for all JUnit tests.
 */

public class BaseTest {
	
	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {

		// Bootstrap the Variant container
		VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream("/variant-junit.props"));
		Variant.bootstrap();

		// (Re)create the schema;
		switch (((EventPersisterJdbc)Variant.getEventWriter().getEventPersister()).getVendor()) {
		case POSTGRES: 
			JdbcUtil.recreateSchema();
			break;
		case H2:
			JdbcUtil.createSchema();  // Fresh in-memory DB.
			break;
		}

	}

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
		InputStream result = PropertiesChain.class.getResourceAsStream(name);
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
	 */
	static protected void assertMatches(String pattern, String string) {
		assertTrue(Pattern.compile(pattern).matcher(string).matches());
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	static protected void assertNotMatches(String pattern, String string) {
		assertTrue(!Pattern.compile(pattern).matcher(string).matches());
	}

	/**
	 * Succeeds if first argument equals to one of the following arguments.
	 * @param actual
	 * @param expected
	 */
	static protected void assertEqualsMulti(Object actual, Object...expected) {
		boolean result = false;
		for (Object e: expected) {
			if (actual.equals(e)) {
				result = true;
				break;
			}
		}
		assertTrue(result);
	}
}
