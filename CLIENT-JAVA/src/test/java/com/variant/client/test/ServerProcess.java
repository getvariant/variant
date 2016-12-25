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
	private ServerProcess() {}
	
	private static Thread proc = null;
	private static InputStream sbtLog;
	
	/**
	 * Start the server in the background. If there's already a process in the background,
	 * throw an exception.
	 * 
	 * @param config
	 */
	public static void start() throws Exception {
		
		start(new HashMap<String,String>());
	}
	
	public static void start(Map<String,String> config) throws Exception {
		
		if (proc != null && proc.isAlive()) throw new RuntimeException("Server thread is alive. Call stop() first");

		proc = new ProcessThread();
		proc.start();
		// Wait for the proc thread to init
		while (sbtLog == null) Thread.sleep(100);
		
		long start = System.currentTimeMillis();
		BufferedReader sbtLogReader = new BufferedReader(new InputStreamReader(sbtLog));
		while(true) {
			if (System.currentTimeMillis() > start + STARTUP_TIMEOUT_MILLIS) {
				throw new RuntimeException("Server failed to startup in [" + STARTUP_TIMEOUT_MILLIS + "] millis");
			}
			String line = null;
			while((line = sbtLogReader.readLine()) != null) {
				System.out.println(String.format("[%s] %s", ServerProcess.class.getSimpleName(), line));
				if (line.contains("Variant Experiment Server release")) return;
			}
		}
	}
	
	/**
	 * Stop the server.
	 */
	public void stop() {
		proc.interrupt();
	}
	
	/**
	 * Background thread which runs the process and blocks on IO from it.
	 */
	private static class ProcessThread extends Thread {
		
		@Override
		public void run() {
			try {

				File svrDir = new File(new File(".."), "SERVER");
				Process rm = Runtime.getRuntime().exec("rm " + svrDir.getAbsolutePath() + "/target/universal/stage/RUNNING_PID");
				rm.waitFor();
				IOUtils.copy(rm.getInputStream(), System.out);
				IOUtils.copy(rm .getErrorStream(), System.out);
				
				/*
				// Example: sbt "testProd -Dvariant.schemas.dir=none"
				StringBuilder buff = new StringBuilder("sbt \"testProd");
				if (config != null) {
					for (Map.Entry<String, String> e: config.entrySet()) {
						buff.append(" ").append("-D").append(e.getKey()).append("=").append(e.getValue());
					}
				}
				buff.append("\"");
				String command = buff.toString();
				LOG.debug("Running server as [" + command + "]");
				*/

				String command = System.getProperty("user.home") + "/Work/sbt/bin/sbt";
				ProcessBuilder pb = new ProcessBuilder();
				pb.command(command, "testProd");
				pb.directory(svrDir);

				Process p = pb.start();
				sbtLog = p.getInputStream();
			}
			catch (Exception e) {
				LOG.error("Exception in proc thread", e);
			}
		}
	}

}
