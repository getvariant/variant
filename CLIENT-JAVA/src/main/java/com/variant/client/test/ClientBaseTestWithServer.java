package com.variant.client.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.variant.core.util.IoUtils;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTestWithServer extends ClientBaseTest {
		
	protected static ServerProcess server = null;
	
	// Junit's implementation of BeforeClass() requires it to be static, so we
	// can't use an abstract method here since it must be static. This value will only
	// work for the java client test because they are in the same directory.
	// the java servlet project will have to override it.
	final private static String defaultPathToServerProject = "../SERVER";
	
	// Server should have initialized this schemata dir
	public final static String SCHEMATA_DIR = "/tmp/schemata-remote";
	
	// Takes this long for the new schema to be detected.
	public final static long dirWatcherLatencyMsecs = 10000;
	
	/**
	 * Start the server once for test case
	 * @throws Exception
	 */
	protected static void startServer(String serverConfig) throws Exception {
		String sysVar = System.getProperty("variant.server.project.dir");
		String exec = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/remoteServerStart.sh";
		String[] procArgs = serverConfig == null ? new String[] {exec} : new String[] {exec, serverConfig};
		server = new ServerProcess(procArgs);
		server.start();
	}

	protected static void startServer() throws Exception {
		
		IoUtils.emptyDir(SCHEMATA_DIR);
		//Deploy the schemata
	    IoUtils.fileCopy("schemata-remote/big-covar-schema.json", SCHEMATA_DIR + "/big-covar-schema.json");
	    IoUtils.fileCopy("schemata-remote/petclinic-schema.json", SCHEMATA_DIR + "/petclinic-schema.json");
		startServer(null);
	}
	
	/**
	 * Start server once per test case.
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		startServer();
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

