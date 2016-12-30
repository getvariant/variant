package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.ClientException;
import com.variant.client.InternalErrorException;
import com.variant.client.TargetingTracker;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.schema.Schema;
import com.variant.core.test.VariantBaseTest;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BaseTest extends VariantBaseTest {

	/**
	 * Build up userData arguments for the *Simple trackers. 
	 * They expect user data as follows:
	 * 	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - {@link TargetingTracker.Entry} objects, if any
	 *  
	 * @param sessionId
	 * @param experiences
	 * @return
	 */
	protected Object[] userDataForSimpleIn(Schema schema, String sessionId, String...experiences) {
		
		if (experiences.length > 0 && schema == null) 
			throw new InternalErrorException("Schema cannot be null if experiences are given");
		
		Object[] result = new Object[experiences.length + 1];
		result[0] = sessionId;
		for (int i = 0; i < experiences.length; i++) {
			result[i+1] = new TargetingTrackerEntryImpl(experience(experiences[i], schema), System.currentTimeMillis());
		}
		
		return result;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                MORE EXCEPTION INTERCEPTORS                                  //
	//---------------------------------------------------------------------------------------------//

	/**
	 *
	 */
	protected static abstract class ClientExceptionInterceptor 
		extends ExceptionInterceptor<ClientException> {
		
		@Override
		final public Class<ClientException> getExceptionClass() {
			return ClientException.class;
		}
		
		/**
		 * Call this if you want assertion always thrown.
		 */
		final public void assertThrown(int code, String message, String comment) throws Exception {
			ClientException result = super.run();
			assertNotNull("Expected exception not thrown", result);
			assertEquals(code, result.getCode());
			assertEquals(message, result.getMessage());
			assertEquals(comment, result.getComment());
		}
		
	}

}

