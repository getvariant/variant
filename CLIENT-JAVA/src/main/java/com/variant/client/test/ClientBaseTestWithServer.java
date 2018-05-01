package com.variant.client.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.variant.core.util.IoUtils;

/**
 * Base class for all Core JUnit tests.
 */
abstract public class ClientBaseTestWithServer extends ClientBaseTest {
		
	// Subclasses can get to the server process here
	protected static ServerProcess server = null;
		
	// Server should have initialized this schemata dir
	public final static String SCHEMATA_DIR = "/tmp/schemata-remote";
	
	// Takes this long for the new schema to be detected.
	public final static int dirWatcherLatencyMillis = 25000;
	
	/**
	 * Start the server once for test case
	 * Additional server-side config parameters may be set to override the default.
	 * @throws Exception
	 */
	protected static void startServer(Map<String, String> svrConf) throws Exception {

		IoUtils.emptyDir(SCHEMATA_DIR);
		//Deploy the schemata
	    IoUtils.fileCopy("schemata-remote/big-covar-schema.json", SCHEMATA_DIR + "/big-covar-schema.json");
	    IoUtils.fileCopy("schemata-remote/petclinic-schema.json", SCHEMATA_DIR + "/petclinic-schema.json");
		server = new ServerProcess();
		server.start(svrConf);
	}

	protected static void startServer() throws Exception {
		startServer(new HashMap<String,String>());
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

