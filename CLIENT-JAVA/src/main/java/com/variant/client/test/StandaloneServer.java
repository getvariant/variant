package com.variant.client.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.util.CollectionsUtils;

/**
 * Start server process in a subprocess.
 * 
 * @author Igor
 */
public class StandaloneServer {

	private static final Logger LOG = LoggerFactory.getLogger(StandaloneServer.class);
	private static final long STARTUP_TIMEOUT_MILLIS = 25000; // give up if server didn't startup in 25 secs;

	// Junit's implementation of BeforeClass() requires it to be static, so we
	// can't use an abstract method here since it must be static. This value will only
	// work for the java client test because they are in the same directory.
	// the java servlet project will have to override it.
	final private static String defaultPathToServerProject = "../SERVER";

	private final String serverDir;
	private final String command;
	
	private StandaloneServerProcessThread svrProc = null;
	private LogReaderThread logReader = null;
	private InputStream sbtOut;
	private InputStream sbtErr;
	private boolean serverUp = false;
	
	
	/**
	 * Build the standalone server in the given file system directory.
	 */
	public StandaloneServer(String serverDir) throws Exception {
		
		this.serverDir = serverDir;
		
		// If we're taking longer than this, something must be amiss.
		final int timeoutSecs = 20;
		
		// Build the process
		String sysVar = System.getProperty("variant.server.project.dir");
		command = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/standaloneServer.sh";
		int rc = NativeProcess.exec(command + " build " + serverDir);
		
		if (rc != 0) {
			throw new RuntimeException("Failed to build standalone server");
		}
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

		String sysVar = System.getProperty("variant.server.project.dir");
		String exec = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/standaloneServer.sh start";
		String[] execArgs = {exec, " "};
		for (Map.Entry<String,String> entry: config.entrySet()) {
			execArgs[1] += "-D" + entry.getKey() + "=" + entry.getValue() + " ";
		}

		if (svrProc != null && svrProc.isAlive()) throw new RuntimeException("Server thread is alive. Call stop() first");
		
		LOG.info(String.format("Starting local server [%s]", CollectionsUtils.toString(execArgs)));
		svrProc = new StandaloneServerProcessThread(execArgs);
		svrProc.start();

		logReader = new LogReaderThread();
		logReader.start();
		
		long start = System.currentTimeMillis();
		while(!serverUp) {
			if (System.currentTimeMillis() > start + STARTUP_TIMEOUT_MILLIS) {
				stop();
				throw new RuntimeException("Server failed to startup in [" + STARTUP_TIMEOUT_MILLIS + "] millis");
			}
			Thread.sleep(100);
		}
	}
	
	/**
	 * Stop the server.
	 */
	public void stop() throws Exception {
		
		if (svrProc == null) return;
		
		// a slight delay to let the log reader have a chance to run one more time and catch up.
		Thread.sleep(100);
		
		String sysVar = System.getProperty("variant.server.project.dir");
		String exec = (sysVar == null ? defaultPathToServerProject : sysVar) + "/mbin/standaloneServer.sh stop";
		String[] execArgs = {exec, " "};
		Runtime.getRuntime().exec(execArgs);
		
		svrProc.destroyProc();
		Thread.sleep(1000);  // Let async cleanup finish.
		logReader.interrupt();
		sbtErr = sbtOut = null;
		serverUp = false;
		LOG.info(String.format("Stopped local server"));
	}

	/**
	 * Restart the server.
	 * @throws Exception 
	 */
	public void restart(Map<String,String> config) throws Exception {
		stop();
		start(config);
	}

	/**
	 * Restart the server.
	 * @throws Exception 
	 */
	public void restart() throws Exception {
		restart(new HashMap<String,String>());
	}

	/**
	 * Background thread which runs the process and blocks on IO from it.
	 */
	private class StandaloneServerProcessThread extends Thread {
		
		private final String[] execArgs;
		private Process sbt;

		private StandaloneServerProcessThread(String[] execArgs) {
			this.execArgs = execArgs;
		}
		
		@Override
		public void run() {
			try {
				sbt = Runtime.getRuntime().exec(execArgs);
				sbtOut = sbt.getInputStream();
				sbtErr = sbt.getErrorStream();
			}
			catch (Exception e) {
				LOG.error("Exception in proc thread", e);
			}
		}
		
		private void destroyProc() {
			sbt.destroy();
		}
	}

	/**
	 * Background server log reader thread.
	 */
	private class LogReaderThread extends Thread {
		
		@Override
		public void run() {
			try {
				BufferedReader outReader = null;				
				BufferedReader errReader = null;				
				while(true && ! isInterrupted()) {
					if (sbtOut != null && sbtErr != null) {
						// Read out
						if (outReader == null)
							outReader = new BufferedReader(new InputStreamReader(sbtOut));
						
						String line = null;
						while((line = outReader.readLine()) != null) {
							System.out.println("<SVR OUT> " + line);
							if (line.contains("Variant Experiment Server release")) {
								Thread.sleep(1500); // The above msg comes up just before the port is bound -> wait.
								serverUp = true;
							}
						}

						// Read error
						if (errReader == null)
							errReader = new BufferedReader(new InputStreamReader(sbtErr));

						while((line = errReader.readLine()) != null) {
							System.err.println("<SVR ERR> " + line);
						}
					}
					Thread.sleep(10);
				}
			}
			catch (Exception e) {}
			LOG.debug("LogReaderTread done");
		}
	}
	
}
