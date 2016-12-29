package com.variant.client.test;

import org.junit.Before;

import com.variant.client.TargetingTracker;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.exception.InternalException;
import com.variant.core.schema.Schema;
import com.variant.core.test.VariantBaseTest;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BaseTest extends VariantBaseTest {
	
		
	/**
	 * Start the server.
	 * @throws Exception
	 */
	@Before
	public void _beforeTestCase() throws Exception {
		
	}

	/**
	 * Subclasses will be able to override this
	 *
	protected VariantClient newBareClient() {
		Injector.setConfigNameAsResource("/variant/injector-bare-client-test.json");
		VariantClientImpl result =  (VariantClientImpl) VariantClient.Factory.getInstance("/variant/bare-client-test.props");
		core = result.getCoreApi();
		return result;
	}
    */
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
			throw new InternalException("Schema cannot be null if experiences are given");
		
		Object[] result = new Object[experiences.length + 1];
		result[0] = sessionId;
		for (int i = 0; i < experiences.length; i++) {
			result[i+1] = new TargetingTrackerEntryImpl(experience(experiences[i], schema), System.currentTimeMillis());
		}
		
		return result;
	}
}

