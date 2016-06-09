package com.variant.core.test;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.PropertiesChain;

/**
 * Common utility methods for all JUnit tests in all projects, hence we've put it in the main scope.
 */

abstract public class BaseTestCommon {
	
	abstract protected JdbcService getJdbcService();
	abstract protected Schema getSchema();
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected void recreateSchema() throws Exception {
		try {
			
			JdbcService jdbc = getJdbcService();
			
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
	protected void printMessages(ParserResponse response) {
		if (response.hasMessages()) 
			for (ParserMessage msg: response.getMessages()) System.out.println(msg);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	protected InputStream openResourceAsInputStream(String name) {
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
	protected Experience experience(String name) {
		String[] tokens = name.split("\\.");
		return getSchema().getTest(tokens[0]).getExperience(tokens[1]);
	}

    protected String targetingTrackerString(String...experiences) {
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
	//                                        ASSERTION                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param args
	 * @return
	 */
	protected void assertMatches(String pattern, String string) {
		assertTrue(
				"Pattern '" + pattern + "' does not match string '" + string + "'", 
				Pattern.compile(pattern).matcher(string).matches());
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	protected void assertNotMatches(String pattern, String string) {
		assertTrue(!Pattern.compile(pattern).matcher(string).matches());
	}

	/**
	 * Succeeds if first argument equals to one of the following arguments.
	 * @param actual
	 * @param expected
	 */
	protected void assertEqualsMulti(Object actual, Object...expected) {
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
	protected <T> void assertEqualAsSets(Collection<T> actual, Collection<T> expected, Comparator<T> comp) {
		
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
	protected <T> void assertEqualAsSets(Collection<T> actual, Collection<T> expected) {

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
	protected <T> void assertEqualAsSets(Collection<T> actual, @SuppressWarnings("unchecked") T...expected) {
		assertEqualAsSets(actual, Arrays.asList(expected));
	}

	/**
	 * Same as above for maps
	 * @param actual
	 * @param expected
	 */
	protected <K,V> void assertEqualAsSets(Map<K,V> actual, Map<K,V> expected) {
		assertEqualAsSets(actual.entrySet(), expected.entrySet());
	}

	/**
	 * Same as above with custom comparator over entries.
	 * @param actual
	 * @param expected
	 */
	protected <K,V> void assertEqualAsSets(Map<K,V> actual, Map<K,V> expected, Comparator<Map.Entry<K, V>> comp) {
		assertEqualAsSets(actual.entrySet(), expected.entrySet(), comp);
	}

	/**
	 * Generic Exception Interceptor
	 *
	 * @param <E>
	 */
	protected static abstract class ExceptionInterceptor<E> {

		abstract public void toRun();
		abstract public Class<E> getExceptionClass();
		
		public void onThrown(E e) {}  // do nothing by default
		public void onNotThrown() {}  // do nothing by default
		
		/**
		 * Call this if you want to analyse both thrown and unthrown cases.
		 * @return
		 */
		final protected E run() throws Exception {
			E result = null;
			try {
				toRun();
			}
			catch (Exception x) {
				if (getExceptionClass().isInstance(x)) {
					result = getExceptionClass().cast(x);
					onThrown(result);
				}
				else throw x;
			}
			if (result == null) onNotThrown();				
			return result;
		}
		
		/**
		 * Call this if you want assertion always thrown.
		 */
		final public void assertThrown() throws Exception {
			assertTrue("Assertion of type [" + Class.class.getName() + "] was not thrown when expected", run() != null);
		}

	}
	
	/**
	 * Exception interceptor for VariantRuntimeException
	 *
	 */
	protected static abstract class VariantRuntimeExceptionInterceptor 
		extends ExceptionInterceptor<VariantRuntimeException> {
		
		@Override
		final public Class<VariantRuntimeException> getExceptionClass() {
			return VariantRuntimeException.class;
		}
		
		/**
		 * Call this if you want assertion always thrown.
		 */
		final public void assertThrown(MessageTemplate template, Object...templateArgs) throws Exception {
			VariantRuntimeException result = super.run();
			assertEquals(new VariantRuntimeException(template, templateArgs).getMessage(), result.getMessage());
		}

	}
}
