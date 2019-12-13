package com.variant.client.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.VariantException;
import com.variant.share.error.UserError;
import com.variant.share.test.VariantBaseTest;

/**
 * Base class for all Client JUnit tests.
 */
public abstract class ClientBaseTest extends VariantBaseTest {
	
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
			assertEquals("Expected exception was not thrown", result.error, error);
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

