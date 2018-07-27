package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.VariantException;
import com.variant.core.UserError;
import com.variant.core.schema.Schema;
import com.variant.core.test.VariantBaseTest;

/**
 * Base class for all Client JUnit tests.
 * TODO: why isn't this in test?
 */
public abstract class ClientBaseTest extends VariantBaseTest {
	
	/**
	 * Build up userData arguments for the *Simple trackers. 
	 * They expect user data as follows:
	 * 	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - Test.Experience objects, if any
	 *  
	 * @param sessionId
	 * @param experiences
	 * @return
	 */
	protected Object[] userDataForSimpleIn(String sessionId, Schema schema, String...experiences) {
		
		Object[] result = new Object[experiences.length + 1];
		result[0] = sessionId;
		for (int i = 0; i < experiences.length; i++) {
			result[i+1] = experience(schema, experiences[i]);
		}
		
		return result;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                MORE EXCEPTION INTERCEPTORS                                  //
	//---------------------------------------------------------------------------------------------//

	/**
	 * User error Intercepter
	 */
	protected static abstract class ClientExceptionInterceptor 
		extends ExceptionInterceptor<VariantException> {
		
		@Override
		final public Class<VariantException> getExceptionClass() {
			return VariantException.class;
		}

		/**
		 * Client side errors: we have access to them at comp time.
		 */
		final public void assertThrown(UserError error, Object...args) {
			VariantException result = super.run();
			assertNotNull("No exception was thrown", result);
			assertEquals("Expected exception was not thrown", result.getError(), error);
		}
		
		/**
		 * Server side errors: We don't have access to them at comp time
		 */
		final public void assertThrown(Class<? extends VariantException> uex) {
			VariantException result = super.run();
			assertNotNull("No exception was thrown", result);
			assertEquals("Expected exception was not thrown", uex, result.getClass());
		}
	}

}

