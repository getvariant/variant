package com.variant.client.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTestWithServer extends ClientBaseTest {
		
	protected static ServerProcess server;
	
	/**
	 * Start the server before each test case
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		server = new ServerProcess();
		server.start();
	}

	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stop();
	}

}

