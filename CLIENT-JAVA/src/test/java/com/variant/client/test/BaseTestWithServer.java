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
public abstract class BaseTestWithServer extends BaseTest {
	
		
	/**
	 * Start the server.
	 * @throws Exception
	 */
	@Before
	public void _beforeTestCase() throws Exception {
		ServerProcess.start();
	}

}

