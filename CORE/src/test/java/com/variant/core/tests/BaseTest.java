package com.variant.core.tests;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.BeforeClass;

import com.variant.core.Variant;
import com.variant.core.config.PropertiesChain;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.jdbc.EventPersisterJdbc;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;

/**
 * Common utility methods for all JUnit tests.
 */

public class BaseTest {
	
	protected static VariantCoreImpl api = (VariantCoreImpl) Variant.Factory.getInstance();

	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {

		// Bootstrap the Variant container
		api.bootstrap("/variant-junit.props");

		// (Re)create the schema;
		switch (((EventPersisterJdbc)api.getEventWriter().getEventPersister()).getVendor()) {
		case POSTGRES: 
			JdbcUtil.recreateSchema();
			break;
		case H2:
			JdbcUtil.createSchema();  // Fresh in-memory DB.
			break;
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                         HELPERS                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Print all messages to std out.
	 * @param response
	 */
	static protected void printMessages(ParserResponse response) {
		if (response.hasMessages()) {
			response.getMessages().forEach(
					new Consumer<ParserMessage>() {
						public void accept(ParserMessage pe) {
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
	 * Pull experience from schema
	 * @param name
	 * @return
	 */
	static protected Experience experience(String name) {
		String[] tokens = name.split("\\.");
		return api.getSchema().getTest(tokens[0]).getExperience(tokens[1]);
	}

	static protected String targetingTrackerString(String...experiences) {
		long timestamp = System.currentTimeMillis();
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (String experienceString: experiences) {
			if (first) first = false;
			else result.append("|");
			result.append(timestamp).
				append(".").
				append(experience(experienceString).toString());
		}
		return result.toString();
	}
	//---------------------------------------------------------------------------------------------//
	//                                         ASSERTS                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param args
	 * @return
	 */
	static protected void assertMatches(String pattern, String string) {
		assertTrue(
				"Pattern '" + pattern + "' does not match string '" + string + "'", 
				Pattern.compile(pattern).matcher(string).matches());
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
