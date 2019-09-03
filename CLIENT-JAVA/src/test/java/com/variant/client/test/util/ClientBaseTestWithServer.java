package com.variant.client.test.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;

import com.variant.client.test.StandaloneServer;
import com.variant.client.test.util.event.TraceEventReader;
import com.variant.core.util.IoUtils;
import com.variant.core.util.StringUtils;

/**
 * Base class for all Client JUnit tests which require a server. (Most of them.)
 * The static before
 */
abstract public class ClientBaseTestWithServer extends ClientBaseTest {
				
	// Remote server location
	protected final static String SERVER_DIR = "/tmp/remote-server";
	
	// Remote server should mount this schemata dir
	protected final static String SCHEMATA_DIR = "/tmp/remote-schemata";
	
	// Schema files location in the local project
	protected final static String SCHEMATA_DIR_SRC = "src/test/resources/schemata-remote/";

	// Event writer max delay hardcoded here. Must match what's in 
	// standalone-server/conf/variant.conf
	protected final static long EVENT_WRITER_MAX_DELAY = 2000;
	
	// Filesystem watcher takes this long to react.
	protected final static int dirWatcherLatencyMillis = 12000;
	
	// Trace event reader. Concrete tests should use this one, instead of initializing their own.
	// This way we can definitively close it, avoiding connection leaks. Note that connection opened
	// in local tests will not be closed after the client application quits. I am not sure why.
	protected final static TraceEventReader traceEventReader = new TraceEventReader();
	
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
		    StandaloneServer result = new StandaloneServer(SERVER_DIR, "postgres");
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
	protected static void restartServer(Map<String,String> config) throws Exception {

		server.stop();
		
		try {
			// Rebuild the schemata dir.
			IoUtils.emptyDir(SCHEMATA_DIR);
			IoUtils.fileCopy(SCHEMATA_DIR_SRC + "monster.schema", SCHEMATA_DIR + "/monster.schema");
		   IoUtils.fileCopy(SCHEMATA_DIR_SRC + "petclinic.schema", SCHEMATA_DIR + "/petclinic.schema");
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
	protected static void restartServer() throws Exception {
		restartServer(new HashMap<String,String>());
	}

	/**
	 * Stop the standalone server.
	 */
	protected static void stopServer() throws Exception{
	   server.stop();
	}

   /**
    * Destroy the standalone server.
    */
   protected static void destroyServer() throws Exception{
      server.destroy();
   }

   @AfterClass
   public static void cleanup() throws Exception {
   
      traceEventReader.close();
      destroyServer();
      IoUtils.delete(SCHEMATA_DIR);
   }    
	
}

