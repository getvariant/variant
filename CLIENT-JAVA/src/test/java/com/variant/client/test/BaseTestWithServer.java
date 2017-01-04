package com.variant.client.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.variant.client.test.util.ServerProcess;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BaseTestWithServer extends BaseTest {
		
	private static ServerProcess svrProc;
	
	/**
	 * Start the server before each test case
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		svrProc = new ServerProcess();
		svrProc.start();
	}

	
	@AfterClass
	public static void afterClass() throws Exception {
		svrProc.stop();
	}

}

