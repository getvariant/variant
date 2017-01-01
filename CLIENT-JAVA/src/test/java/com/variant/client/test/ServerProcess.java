package com.variant.client.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerProcess {

	private static final Logger LOG = LoggerFactory.getLogger(ServerProcess.class);
	private static final long STARTUP_TIMEOUT_MILLIS = 20000; // give up if server didn't startup in 10 secs;
	
	/**
	 * Static singleton.
	 */
	 public ServerProcess() {}
	
	private Thread svrProc = null;
	private Thread logReader = null;
	private InputStream sbtLog;
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

		svrProc = new ProcessThread();
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
		svrProc.interrupt();
		logReader.interrupt();
	}
	
	/**
	 * Background thread which runs the process and blocks on IO from it.
	 */
	private class ProcessThread extends Thread {
		
		@Override
		public void run() {
			try {
				File svrDir = new File(new File(".."), "SERVER");
				Process rm = Runtime.getRuntime().exec("rm " + svrDir.getAbsolutePath() + "/target/universal/stage/RUNNING_PID");
				rm.waitFor();
				IOUtils.copy(rm.getInputStream(), System.out);
				IOUtils.copy(rm .getErrorStream(), System.out);
				
				String command = System.getProperty("user.home") + "/Work/sbt/bin/sbt";
				ProcessBuilder pb = new ProcessBuilder();
				pb.command(command, "testProd -Dvariant.config.file=conf-test/variant.conf");
				pb.directory(svrDir);

				Process p = pb.start();
				sbtLog = p.getInputStream();
			}
			catch (Exception e) {
				LOG.error("Exception in proc thread", e);
			}
		}
	}

	/**
	 * Background server log reader thread.
	 */
	private class LogReaderThread extends Thread {
		
		@Override
		public void run() {
			try {
				BufferedReader sbtLogReader = null;				
				while(true) {
					if (sbtLog != null) {
						if (sbtLogReader == null)
							sbtLogReader = new BufferedReader(new InputStreamReader(sbtLog));
						String line = null;
						while((line = sbtLogReader.readLine()) != null) {
							System.out.println(String.format("[%s] %s", ServerProcess.class.getSimpleName(), line));
							if (line.contains("Variant Experiment Server release")) {
								Thread.sleep(300);  // Wait for the port to open.
								serverUp = true;
							}
						}
					}
					Thread.sleep(100);
				}
			}
			catch (Exception e) {
				LOG.error("Exception in log reader thread", e);
			}
		}
	}
	
}
