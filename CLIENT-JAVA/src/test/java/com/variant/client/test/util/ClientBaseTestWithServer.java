package com.variant.client.test.util;

import java.util.HashMap;
import java.util.Map;

import com.variant.client.test.StandaloneServer;
import com.variant.core.util.IoUtils;
import com.variant.core.util.StringUtils;

/**
 * Base class for all Client JUnit tests which require a server. (Most of them.)
 * The static before
 */
abstract public class ClientBaseTestWithServer extends ClientBaseTest {
				
	// Remote server should mount this schemata dir
	public final static String SERVER_DIR = "/tmp/remote-server";

	// Remote server should mount this schemata dir
	public final static String SCHEMATA_DIR = "/tmp/remote-schemata";
	
	// Schema files location in the local project
	public final static String SCHEMATA_DIR_SRC = "src/test/resources/schemata-remote/";

	// Filesystem watcher takes this long to react.
	public final static int dirWatcherLatencyMillis = 12000;
	
	// JUnits's way is to run the static @BeforeClass for each instantiation,
	// i.e. for each test. That's not what we want. We'll build the server
	// once for all tests in the final Class.
	private static final StandaloneServer server = buildServer();

	/**
	 * Build the standalone server
	 * @throws Exception
	 */
	protected static StandaloneServer buildServer() {

		try {
		    StandaloneServer result = new StandaloneServer(SERVER_DIR);
		    String msg = "Built Standalone Server in " + SERVER_DIR;
		    int width = msg.length() + 10;
		    System.out.println("       " + StringUtils.repeat("*", width));
		    System.out.println("       **** " + msg + " ****");
		    System.out.println("       " + StringUtils.repeat("*", width));
		    return result;
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to build server", e);
		}

	}

	/**
	 * Restart the standalone server. We don't provide a plain start method, just so
	 * the caller always gets the clean slate.
	 * 
	 * @param config Additional server-side config parameters may be set to override the default.
	 */
	protected static void restartServer(Map<String,String> config) {

		stopServer();
		
		try {
			// Rebuild the schemata dir.
			IoUtils.emptyDir(SCHEMATA_DIR);
		    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "big-conjoint-schema.json", SCHEMATA_DIR + "/big-conjoint-schema.json");
		    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "petclinic-schema.json", SCHEMATA_DIR + "/petclinic-schema.json");
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to start server", e);
		}

		@SuppressWarnings("serial")
		Map<String,String> _config = new HashMap<String,String>() {{
			put("variant.schemata.dir", SCHEMATA_DIR);
		}};
		if (config != null) _config.putAll(config);
		try {
			server.start(_config);
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to start server", e);
		}

	}
	
	/**
	 * Start the standalone server convenience method.
	 * Additional server-side config parameters may be set to override the default.
	 */
	protected static void restartServer() {
		restartServer(new HashMap<String,String>());
	}

	/**
	 * Stop the standalone server.
	 * Additional server-side config parameters may be set to override the default.
	 */
	protected static void stopServer() {
		try {
			server.stop();
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to stop server", e);
		}
	}

}

