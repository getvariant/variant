package com.variant.client.test;

import org.junit.Before;

import com.variant.client.VariantClient;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.VariantCore;
import com.variant.core.util.inject.Injector;
import com.variant.core.xdm.Schema;
import com.variant.server.test.util.CoreBaseTest;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BareClientBaseTest extends CoreBaseTest {
	
	private static Boolean sqlSchemaCreated = false;

	private VariantCore core = null;
	
	/**
	 * Subclasses will override this
	 * @return
	 */
	protected VariantCore getCoreApi() {
		return core;
	}
	
	/**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 */
	@Before
	public void _beforeTestCase() throws Exception {
		
		synchronized (sqlSchemaCreated) {     // once per JVM
			if (!sqlSchemaCreated) {
				recreateSchema(getCoreApi());
				sqlSchemaCreated = true;
			}
		}
	}
	
	/**
	 * Subclasses will be able to override this
	 */
	protected VariantClient newBareClient() {
		Injector.setConfigNameAsResource("/variant/injector-bare-client-test.json");
		VariantClientImpl result =  (VariantClientImpl) VariantClient.Factory.getInstance("/variant/bare-client-test.props");
		core = result.getCoreApi();
		return result;
	}

	/**
	 * Build up userData arguments for the *Simple trackers. 
	 * They expect user data as follows:
	 * 	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - {@link VariantTargetingTracker.Entry} objects, if any
	 *  
	 * @param sessionId
	 * @param experiences
	 * @return
	 */
	protected Object[] userDataForSimpleIn(Schema schema, String sessionId, String...experiences) {
		
		if (experiences.length > 0 && schema == null) 
			throw new VariantInternalException("Schema cannot be null if experiences are given");
		
		Object[] result = new Object[experiences.length + 1];
		result[0] = sessionId;
		for (int i = 0; i < experiences.length; i++) {
			result[i+1] = new TargetingTrackerEntryImpl(experience(experiences[i], schema), System.currentTimeMillis());
		}
		
		return result;
	}
}

