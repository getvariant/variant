package com.variant.client.test;

import org.junit.AfterClass;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTestWithServer extends ClientBaseTest {
		
	protected static ServerProcess server;
	
	// Junit's implementation of BeforeClass() requires it to be static, so we
	// can't use an abstract method here since it must be static. This value will only
	// work for the java client test because they are in the same directory.
	// the java servlet project will have to override it.
	final private static String defaultPathToServerProject = "../SERVER";
	
	final private static String defaultServerConfig = "conf-test/variant-testProd.conf";
	
	/**
	 * Start the server once for test case
	 * @throws Exception
	 */
	protected void startServer(String serverConfig) throws Exception {
		String sysVar = System.getProperty("variant.server.project.dir");
		String exec = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/startServer.sh";
		String conf = serverConfig == null ? defaultServerConfig : serverConfig;
		server = new ServerProcess(new String[] {exec, conf});
		server.start();
	}

	protected void startServer() throws Exception {
	   startServer(null);
	}
	
	/**
	 * Kill server is still up at end of test case.
	 * @throws Exception
	 */
	@AfterClass
	public static void afterClass() throws Exception {
		if (server != null) server.stop();
	}

}

