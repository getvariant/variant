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
	protected static StandaloneServer server = null;
		
	// Remote server should mount this schemata dir
	public final static String SERVER_DIR = "/tmp/remote-server";

	// Remote server should mount this schemata dir
	public final static String SCHEMATA_DIR = "/tmp/remote-schemata";
	
	// Schema files location in the local project
	public final static String SCHEMATA_DIR_SRC = "src/test/resources/schemata-remote/";

	// Filesystem watcher takes this long to react.
	public final static int dirWatcherLatencyMillis = 12000;
	
	/**
	 * Start the server once for test case
	 * Additional server-side config parameters may be set to override the default.
	 * @throws Exception
	 */
	protected static void startServer(Map<String, String> svrConf) throws Exception {

		//Deploy the schemata
	    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "big-conjoint-schema.json", SCHEMATA_DIR + "/big-conjoint-schema.json");
	    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "petclinic-schema.json", SCHEMATA_DIR + "/petclinic-schema.json");
		server = new StandaloneServer(SERVER_DIR);
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

