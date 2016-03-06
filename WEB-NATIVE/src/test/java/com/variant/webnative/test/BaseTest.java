package com.variant.webnative.test;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.BeforeClass;

import com.variant.core.config.PropertiesChain;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.webnative.VariantWebnative;
import com.variant.webnative.VariantWebnativeTestFacade;

/**
 * Common utility methods for all JUnit tests.
 */

public class BaseTest {
	
	protected static VariantWebnative api = null;

	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {
		rebootApi();
		recreateSchema();
	}

	/**
	 * @throws Exception 
	 * 
	 */
	protected static void rebootApi() throws Exception {
		api = new VariantWebnative("/variant-test.props");
	}

	/**
	 * @throws Exception 
	 * 
	 */
	protected static void recreateSchema() throws Exception {
		try {
			
			JdbcService jdbc = new JdbcService(VariantWebnativeTestFacade.getCoreApi(api));
			
			switch (jdbc.getVendor()) {
			case POSTGRES: 
				jdbc.recreateSchema();
				break;
			case H2:
				jdbc.createSchema();  // Fresh in-memory DB.
				break;
			}
		}
		catch (ClassCastException e) {/*OK*/}		
		catch (Exception e) { throw e; }		

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
	
	/**
	 * Assert that two collections are set-equivalent, i.e.
	 * for each element in one, there's an equal element in the other.
	 * Custom comparator.
	 *  
	 * @param 
	 */	
	static protected <T> void assertEqualAsSets(Collection<T> actual, Collection<T> expected, Comparator<T> comp) {
		
		for (T a: actual) {
			boolean found = false;
			for (T e: expected) {
				if (comp.compare(a,e) == 0) {
					found = true;
					break;
				}
			}
			assertTrue("Actual element " + a + " not found among expected elements", found);
		}
		
		for (T e: expected) {
			boolean found = false;
			for (T a: actual) {
				if (comp.compare(a,e) == 0) {
					found = true;
					break;
				}
			}
			assertTrue("Expected element " + e + " not found among actual elements", found);
		}
	}

	/**
	 * Same as above with the trivial comparator
	 *  
	 * @param 
	 */	
	static protected <T> void assertEqualAsSets(Collection<T> actual, Collection<T> expected) {

		Comparator<T> comp = new Comparator<T>() {
			@Override
			public int compare(Object o1, Object o2) {return o1.equals(o2) ? 0 : 1;}
		};
		assertEqualAsSets(actual, expected, comp);
	}

	/**
	 * Same as above for varargs
	 * @param actual
	 * @param expected
	 */
	@SafeVarargs
	static protected <T> void assertEqualAsSets(Collection<T> actual, T...expected) {
		assertEqualAsSets(actual, Arrays.asList(expected));
	}

	/**
	 * Same as above for maps
	 * @param actual
	 * @param expected
	 */
	static protected <K,V> void assertEqualAsSets(Map<K,V> actual, Map<K,V> expected) {
		assertEqualAsSets(actual.entrySet(), expected.entrySet());
	}

	/**
	 * Same as above with custom comparator over entries.
	 * @param actual
	 * @param expected
	 */
	static protected <K,V> void assertEqualAsSets(Map<K,V> actual, Map<K,V> expected, Comparator<Map.Entry<K, V>> comp) {
		assertEqualAsSets(actual.entrySet(), expected.entrySet(), comp);
	}

}
