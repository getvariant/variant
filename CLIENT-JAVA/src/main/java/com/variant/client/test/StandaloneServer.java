package com.variant.client.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start server process in a subprocess.
 * This is main so that adapters can take advantate of it.
 * 
 * @author Igor
 */
public class StandaloneServer {

	private static final Logger LOG = LoggerFactory.getLogger(StandaloneServer.class);
	private static final long STARTUP_TIMEOUT_SECS = 20; // give up if server didn't startup in a minute.

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
	public StandaloneServer(String serverDir, String flusher) throws Exception {
		
		this.serverDir = serverDir;
				
		// The script
		String sysVar = System.getProperty("variant.server.project.dir");
		script = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/standaloneServer.sh";
		
		// Run the command
		String command = script + " build " + serverDir;
		LOG.info(String.format("Building standalone server [%s]", command));
		int rc = NativeProcess.execQuiet(script + " " + serverDir + " " + flusher);

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
	 * Start the async server. If one's already running, it will be killed.
	 * 
	 * @param config
	 */
	public void start(Map<String,String> config) throws Exception {
		
		// Build the command
		String command = serverDir + "/bin/variant.sh start";
		for (Map.Entry<String,String> entry: config.entrySet()) {
			command += " -D" + entry.getKey() + "=" + entry.getValue();
		}
		
		LOG.info(String.format("Starting standalone server [%s]", command));
		
		// An object instead of a primitive to get around Java's lack of support for closures.
		final AtomicBoolean instantiated = new AtomicBoolean(false);

		server  = NativeProcess.start(
				command,
				line -> {
					System.out.println("<OUT> " + line);
					if (line.matches(".*Variant AIM Server .* bootstrapped .*")) instantiated.set(true);
				}, 
				line -> {
					System.out.println("<ERR> " + line); 					
				});

		
		// Wait for the server to come up. There's a race condition between this thread and the thread
		// insiide the server process which reads the subprocess's output to this process's output.
		// We want to block this method until BOTH the server output has been fully transmitted and it
		// has bound to the port and listens to connections.
		boolean listens = false;
		long start = System.currentTimeMillis();
		while(
				(System.currentTimeMillis() < start + STARTUP_TIMEOUT_SECS*1000) && 
				server.isAlive() && 
				!(listens && instantiated.get())) {
			
			if (NativeProcess.execSilent("curl http://localhost:5377") == 0) listens = true;
			//System.out.println("*** " + listens + ", " + instantiated.get());			
			Thread.sleep(200);
		}
		
		if (!listens || !instantiated.get()) {
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
		if (NativeProcess.exec(command) != 0) 
			throw new RuntimeException("Unable to stop server");
	}
}
