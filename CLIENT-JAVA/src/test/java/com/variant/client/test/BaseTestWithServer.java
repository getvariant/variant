package com.variant.client.test;

import org.junit.BeforeClass;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BaseTestWithServer extends BaseTest {
		
	/**
	 * Start the server before each test case
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		ServerProcess.start();
	}

}

