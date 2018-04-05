package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.ClientException;
import com.variant.core.UserError;
import com.variant.core.schema.Schema;
import com.variant.core.test.VariantBaseTest;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTest extends VariantBaseTest {
	
	@Override
	protected Schema getSchema() {
		throw new RuntimeException("No schema yet");
	}

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
	protected Object[] userDataForSimpleIn(String sessionId, String...experiences) {
		
		Object[] result = new Object[experiences.length + 1];
		result[0] = sessionId;
		for (int i = 0; i < experiences.length; i++) {
			result[i+1] = experience(experiences[i]);
		}
		
		return result;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                MORE EXCEPTION INTERCEPTORS                                  //
	//---------------------------------------------------------------------------------------------//

	/**
	 * User error Intercepter
	 */
	protected static abstract class ClientUserExceptionInterceptor 
		extends ExceptionInterceptor<ClientException.User> {
		
		@Override
		final public Class<ClientException.User> getExceptionClass() {
			return ClientException.User.class;
		}

		/**
		 * Client side errors: we have access to them at comp time.
		 */
		final public void assertThrown(UserError error, Object...args) throws Exception {
			ClientException.User result = super.run();
			assertNotNull("Expected exception not thrown", result);
			assertEquals(result.getError(), error);
		}
		
		/**
		 * Server side errors: We don't have access to them at comp time
		 */
		final public void assertThrown(Class<? extends ClientException.User> uex) throws Exception {
			ClientException.User result = super.run();
			assertNotNull("Expected exception not thrown", result);
			assertEquals(uex, result.getClass());
		}
	}

}

