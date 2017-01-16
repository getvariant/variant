package com.variant.client.test.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerProcess {

	private static final Logger LOG = LoggerFactory.getLogger(ServerProcess.class);
	private static final long STARTUP_TIMEOUT_MILLIS = 25000; // give up if server didn't startup in 25 secs;
	
	/**
	 * Static singleton.
	 */
	 public ServerProcess() {}
	
	private SbtThread svrProc = null;
	private LogReaderThread logReader = null;
	private InputStream sbtOut;
	private InputStream sbtErr;
	private boolean serverUp = false;
	
	/**
	 * Start the server in the background. If there's already a process in the background,
	 * throw an exception.
	 * 
	 * @param config
	 */
	public void start() throws Exception {
		
		start(new HashMap<String,String>());
	}
	
	public void start(Map<String,String> config) throws Exception {
		
		if (svrProc != null && svrProc.isAlive()) throw new RuntimeException("Server thread is alive. Call stop() first");
		svrProc = new SbtThread();
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
	public void stop() {
		// a slight delay to let the log reader have a chance to run one more time and catch up.
		try {Thread.sleep(100);} catch(Throwable t) {}
		svrProc.destroyProc();
		logReader.interrupt();
		sbtErr = sbtOut = null;
		serverUp = false;
	}

	/**
	 * Restart the server.
	 * @throws Exception 
	 */
	public void restart() throws Exception {
		stop();
		Thread.sleep(1000);  // Let async cleanup finish.
		start();
	}

	/**
	 * Background thread which runs the process and blocks on IO from it.
	 */
	private class SbtThread extends Thread {
		
		private Process sbt;
		
		@Override
		public void run() {
			try {
				sbt = Runtime.getRuntime().exec("bin/startServer.sh");
				sbtOut = sbt.getInputStream();
				sbtErr = sbt.getErrorStream();
			}
			catch (Exception e) {
				LOG.error("Exception in proc thread", e);
			}
			LOG.debug("ProcessTread done");
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
				BufferedReader sbtOutReader = null;				
				BufferedReader sbtErrReader = null;				
				while(true && ! isInterrupted()) {
					if (sbtOut != null && sbtErr != null) {
						// Read out
						if (sbtOutReader == null)
							sbtOutReader = new BufferedReader(new InputStreamReader(sbtOut));
						
						String line = null;
						while((line = sbtOutReader.readLine()) != null) {
							System.out.println("<SBT OUT> " + line);
							if (line.contains("Variant Experiment Server release")) {
								Thread.sleep(1500); // The above msg comes up just before the port is bound -> wait.
								serverUp = true;
							}
						}

						// Read error
						if (sbtErrReader == null)
							sbtErrReader = new BufferedReader(new InputStreamReader(sbtErr));

						while((line = sbtErrReader.readLine()) != null) {
							System.err.println("<SBT ERR> " + line);
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
