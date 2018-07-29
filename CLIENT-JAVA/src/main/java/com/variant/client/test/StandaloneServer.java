package com.variant.client.test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start server process in a subprocess.
 * 
 * @author Igor
 */
public class StandaloneServer {

	private static final Logger LOG = LoggerFactory.getLogger(StandaloneServer.class);
	private static final long STARTUP_TIMEOUT_SECS = 60; // give up if server didn't startup in a minute.

	// Junit's implementation of BeforeClass() requires it to be static, so we
	// can't use an abstract method here since it must be static. This value will only
	// work for the java client test because they are in the same directory.
	// the java servlet project will have to override it.
	final private static String defaultPathToServerProject = "../SERVER";

	private final String serverDir;
	private final String script;
	public NativeProcess server;	
	
	/**
	 * Build the standalone server in the given file system directory.
	 */
	public StandaloneServer(String serverDir) throws Exception {
		
		this.serverDir = serverDir;
				
		// The script
		String sysVar = System.getProperty("variant.server.project.dir");
		script = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/standaloneServer.sh";
		
		// Run the command
		String command = script + " build " + serverDir;
		LOG.info(String.format("Building standalone server [%s]", command));
		int rc = NativeProcess.execQuiet(script + " build " + serverDir);

		if (rc != 0) {
			throw new RuntimeException("Failed to build standalone server");
		}
		
		LOG.info("Built standalone server at " + serverDir);

	}

	/**
	 * Start the async standalone server with default params.
	 * @throws Exception
	 */
	public void start() throws Exception {
		start(new HashMap<String,String>());
	}
	
	/**
	 * Start the async server. If already running, though an exception.
	 * 
	 * @param config
	 */
	public void start(Map<String,String> config) throws Exception {

		// Stop server, in case it's running.
		stop();
		
		// Build the command
		String command = serverDir + "/bin/variant.sh start";
		for (Map.Entry<String,String> entry: config.entrySet()) {
			command += " -D" + entry.getKey() + "=" + entry.getValue();
		}
		
		LOG.info(String.format("Starting standalone server [%s]", command));
		
		server  = NativeProcess.start(command);
		
		boolean isUp = false;
		long start = System.currentTimeMillis();
		while((System.currentTimeMillis() < start + STARTUP_TIMEOUT_SECS*1000) && server.isAlive() && !isUp) {
			if (NativeProcess.execSilent("curl http://localhost:5377/variant") == 0) isUp = true;
			Thread.sleep(200);
		}
		
		if (!isUp) {
			server.destroy();
			throw new RuntimeException("Server failed to startup in " + STARTUP_TIMEOUT_SECS + " seconds");
		}

	}
	
	/**
	 * Stop the server.
	 */
	public void stop() throws Exception {
		String command = serverDir + "/bin/variant.sh stop";
		LOG.info("Stopping standalone server [" + command + "]");
		if (NativeProcess.execQuiet(command) != 0) 
			throw new RuntimeException("Unable to stop server");
	}

	/**
	 * Restart the server.
	 * @throws Exception 
	 *
	public void restart(Map<String,String> config) throws Exception {
		stop();
		start(config);
	}

	/**
	 * Restart the server.
	 * @throws Exception 
	 *
	public void restart() throws Exception {
		restart(new HashMap<String,String>());
	}
	*/
}
