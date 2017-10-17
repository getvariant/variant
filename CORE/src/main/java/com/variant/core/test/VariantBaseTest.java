package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import com.variant.core.CommonError;
import com.variant.core.CoreException;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.VariantStringUtils;


/**
 * Common utility methods for all JUnit tests in all projects, hence we've put it in the main scope.
 */

abstract public class VariantBaseTest {
	
	private final static Random rand = new Random(System.currentTimeMillis());
	
	abstract protected Schema getSchema();

	/**
	 * @param ssn The session which will receive this stabile.
	 * @param experiences are expected as "test.exp" 
	 * @return
	 */
    protected void setTargetingStabile(CoreSession ssn, String...experiences) {
		SessionScopedTargetingStabile stabile = new SessionScopedTargetingStabile();
		for (String e: experiences) {
			Experience exp = experience(e);
			stabile.add(exp);
			//((CoreSessionImpl)ssn).addTraversedTest(exp.getTest());
		}
		((CoreSession)ssn).setTargetingStabile(stabile);
	}

	//---------------------------------------------------------------------------------------------//
	//                                         HELPERS                                             //
	//---------------------------------------------------------------------------------------------//

    /**
     * Generate a new random session ID.
     */
    protected String newSid() { 
    	return VariantStringUtils.random64BitString(rand);
    }

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
		InputStream result = VariantBaseTest.class.getResourceAsStream(name);
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

	//---------------------------------------------------------------------------------------------//
	//                                        ASSERTION                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param pattern
	 * @param string
	 * @return
	 */
	protected static void assertMatches(String regex, String string) {
		assertTrue(
				"Regular expression '" + regex + "' does not match string '" + string + "'", 
				Pattern.compile(regex).matcher(string).matches());
	}

	/**
	 * 
	 * @return
	 */
	protected static void assertNotMatches(String regex, String string) {
		assertTrue(
				"Regular expression '" + regex + "' matches string '" + string + "'",
				!Pattern.compile(regex).matcher(string).matches());
	}

	/**
	 * Succeeds if first argument equals to one of the following arguments.
	 * @param actual
	 * @param expected
	 */
	protected static void assertEqualsMulti(Object actual, Object...expected) {
		boolean result = false;
		for (Object e: expected) {
			if (actual.equals(e)) {
				result = true;
				break;
			}
		}
		assertTrue("[" + actual + "] did not equal any of the supplied varargs", result);
	}
	
	/**
	 * Assert two Parser messages are equal
	 * @param expected
	 * @param actual
	 */
	protected static void assertMessageEqual(ParserMessage expected, ParserMessage actual) {
		
		org.junit.Assert.assertEquals(expected.getCode(), actual.getCode());
		org.junit.Assert.assertEquals(expected.getText(), actual.getText());
		org.junit.Assert.assertEquals(expected.getSeverity(), actual.getSeverity());
		org.junit.Assert.assertEquals(expected.getLocation(), actual.getLocation());
	}
	
	/**
	 * Assert that two collections are set-equivalent, i.e.
	 * for each element in one, there's an equal element in the other.
	 * Custom comparator.
	 *  
	 * @param 
	 */
	protected static <T> void assertEqualAsSets(Collection<T> actual, Collection<T> expected, Comparator<T> comp) {
		
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
	protected static <T> void assertEqualAsSets(Collection<T> actual, Collection<T> expected) {

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

	//---------------------------------------------------------------------------------------------//
	//                                  EXCEPTION INTERCEPTORS                                     //
	//---------------------------------------------------------------------------------------------//

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
				else {
					throw x;
				}
			}
			if (result == null) onNotThrown();				
			return result;
		}
		
		/**
		 * Call this if you want assertion always thrown.
		 */
		final public void assertThrown() throws Exception {
			assertTrue("Assertion of type [" + getExceptionClass().getName() + "] was not thrown when expected", run() != null);
		}

	}
	
	/**
	 * Concrete exception intercepter for VariantRuntimeException
	 *
	 */
	protected static abstract class CoreUserExceptionInterceptor 
		extends ExceptionInterceptor<CoreException.User> {
		
		@Override
		final public Class<CoreException.User> getExceptionClass() {
			return CoreException.User.class;
		}
		
		/**
		 * Call this if you want assertion always thrown.
		 */
		final public void assertThrown(CommonError template, Object...args) throws Exception {
			CoreException.User result = super.run();
			assertNotNull("Expected exception not thrown", result);
			assertEquals(new CoreException.User(template, args).getMessage(), result.getMessage());
		}
		
	}
	
	/**
	 * Concrete exception intercepter for VariantInternalException
	 *
	 */
	protected static abstract class CoreInternalExceptionInterceptor 
		extends ExceptionInterceptor<CoreException.Internal> {
		
		@Override
		final public Class<CoreException.Internal> getExceptionClass() {
			return CoreException.Internal.class;
		}
		
		/**
		 * Call this if you want assertion always thrown.
		 */
		final public void assertThrown(String format, Object...args) throws Exception {
			CoreException.Internal result = super.run();
			assertNotNull("Expected exception not thrown", result);
			assertEquals(String.format(format, args), result.getMessage());
		}
	}

	/**
	 * Concrete exception intercepter for IllegalArgumentException
	 *
	 *
	protected static abstract class IllegalArgumentExceptionInterceptor 
		extends ExceptionInterceptor<IllegalArgumentException> {
		
		@Override
		final public Class<IllegalArgumentException> getExceptionClass() {
			return IllegalArgumentException.class;
		}		
	}
*/
}
