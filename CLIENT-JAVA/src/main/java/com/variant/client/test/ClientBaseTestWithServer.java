package com.variant.client.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTestWithServer extends ClientBaseTest {
		
	protected static ServerProcess server;
	
	// Junit's implementation of BeforeClass() requires it to be static, so we
	// can't use an abstract method here since it must be static. This value will only
	// work for the java client test because they are in the same directory.
	// the java servlet project will have to override it.
	protected static String defaultPathToServerProject = "../SERVER";
	
	/**
	 * Start the server before each test case
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		String sysVar = System.getProperty("variant.server.project.dir");
		server = new ServerProcess(sysVar == null ? defaultPathToServerProject : sysVar);
		server.start();
	}

	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stop();
	}

}

