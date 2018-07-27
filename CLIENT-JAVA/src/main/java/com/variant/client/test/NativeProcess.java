package com.variant.client.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class on top of Java's own Process.
 * Start the native process asynchronously, with something useful to do about the process's i/o.
 * 
 * @author Igor
 */
public class NativeProcess {

	private static final Logger LOG = LoggerFactory.getLogger(NativeProcess.class);
	
	@FunctionalInterface
	public static interface StringToUnit {
		void apply(String line);
	}
	
	/**
	 * Factory method starts an asynchronous process and return without waiting for completion.
	 */
	public static NativeProcess start(
			String command,                 // OS (bash) command to execute
			StringToUnit outConsumer,       // Stdout processor
			StringToUnit errorConsumer      // Stderr processor
			) throws Exception {
		
		return new NativeProcess(command, outConsumer, errorConsumer);
	}

	/**
	 * Convenience method. Sends std out and std error to the console.
	 */
	public static NativeProcess start(String command) throws Exception {
		
		return new NativeProcess(
				command, 
				(String line) -> {
					System.out.println("<OUT> " + line); 
				}, 
				(String line) -> {
					System.out.println("<ERR> " + line); 					
				});
	}
	
	/**
	 * Convenience method. Sends std out and std error to the console.
	 * Blocks until process is completed. Returns status.
	 */
	public static int exec(String command) throws Exception {
		
		NativeProcess p = start(command);
		while (p.proc.isAlive()) Thread.sleep(100);
		return p.proc.exitValue();
	}

	// Instance members
	private final Process proc;
	//private final OutConsumerThread outConsumerThread;
	
	/**
	 * Create and start the process in a separate thread.
	 * @param command
	 * @param outConsumer
	 * @param errorConsumer
	 * @throws IOException 
	 */
	private NativeProcess(String command, StringToUnit outConsumer, StringToUnit errorConsumer) throws IOException {

		proc = new ProcessBuilder(command.split("\\s+")).start();
		new OutConsumerThread(outConsumer, errorConsumer).start();
	}

	/**
	 * Separate thread consumes the process' output streams.
	 */
	private class OutConsumerThread extends Thread {
		
		private final StringToUnit outConsumer;
		private final StringToUnit errorConsumer;
		
		private OutConsumerThread(StringToUnit outConsumer, StringToUnit errorConsumer) {
			this.outConsumer = outConsumer;
			this.errorConsumer = errorConsumer;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader outReader = null;				
				BufferedReader errReader = null;
				
				while(true && ! isInterrupted()) {
					
					InputStream stdout = proc.getInputStream();
					InputStream stderr = proc.getErrorStream();
					
					if (stdout != null && stderr != null) {

						// Read out
						if (outReader == null)
							outReader = new BufferedReader(new InputStreamReader(stdout));
						
						String line = null;
						while((line = outReader.readLine()) != null) outConsumer.apply(line);
					

						// Read error
						if (errReader == null)
							errReader = new BufferedReader(new InputStreamReader(stderr));

						while((line = errReader.readLine()) != null) errorConsumer.apply(line);
						
					}
					Thread.sleep(100);
					
					// Shutdown this thread if the native process is completed.
					if (!proc.isAlive()) return;
				}
			}
			catch (Exception e) {}
		}
	}
	
}
