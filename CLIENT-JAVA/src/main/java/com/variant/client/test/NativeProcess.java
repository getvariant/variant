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
				line -> {
					System.out.println("<OUT> " + line); 
				}, 
				line -> {
					System.out.println("<ERR> " + line); 					
				});
	}
	
	/**
	 * Convenience method. Sends std out and std error to this System.{our,err}
	 * respectively. Blocks until process is completed. Returns status.
	 * 
	 * @return completion code of the subprocess.
	 */
	public static int exec(String command) throws Exception {
		
		NativeProcess p = start(command);
		while (p.proc.isAlive()) Thread.sleep(250);
		return p.proc.exitValue();
	}

	/**
	 * Convenience method. Ignores subprocess's both standard out and err.
	 * Blocks until process is completed. Returns status.
	 * 
	 * @return completion code of the subprocess.
	 */
	public static int execSilent(String command) throws Exception {
		
		NativeProcess p = start(
				command,
				line -> {},
				line -> {});
		
		while (p.proc.isAlive()) Thread.sleep(250);
		return p.proc.exitValue();
	}

	/**
	 * Convenience method. Ignores subprocess's standard out and 
	 * connects subprocess's standard err to that of the caller.
	 * Blocks until process is completed. Returns status.
	 * 
	 * @return completion code of the subprocess.
	 */
	public static int execQuiet(String command) throws Exception {
		
		NativeProcess p = start(
				command,
				line -> {},
				line -> {
					System.out.println("<ERR> " + line); 					
				});
		
		while (p.proc.isAlive()) Thread.sleep(250);
		return p.proc.exitValue();
	}

	// Instance members
	private final Process proc;
	private final OutConsumerThread outConsumerThread;
	
	/**
	 * Create and start the process in a separate thread.
	 * @param command
	 * @param outConsumer
	 * @param errorConsumer
	 * @throws IOException 
	 */
	private NativeProcess(String command, StringToUnit outConsumer, StringToUnit errorConsumer) throws IOException {

		// Start the process
		proc = new ProcessBuilder(command.split("\\s+")).start();
		
		// Connect the process's output streams
		outConsumerThread = new OutConsumerThread(outConsumer, errorConsumer);
		outConsumerThread.start();
	}

	/**
	 * Destroy this process.
	 */
	public void destroy() {
		outConsumerThread.interrupt();
		proc.destroy();
	}

	/**
	 */
	public boolean isAlive() {
		return proc.isAlive();
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
				
				while(! isInterrupted()) {
					
					InputStream stdout = proc.getInputStream();
					InputStream stderr = proc.getErrorStream();
					
					if (stdout != null && stderr != null) {

						// Read out
						if (outReader == null)
							outReader = new BufferedReader(new InputStreamReader(stdout));
						
						String line = null;
						while((line = outReader.readLine()) != null) {
							outConsumer.apply(line);
						}
					

						// Read error
						if (errReader == null)
							errReader = new BufferedReader(new InputStreamReader(stderr));

						while((line = errReader.readLine()) != null) errorConsumer.apply(line);
						
					}
					Thread.sleep(100);
					
					// Shutdown this thread if the native process is completed.
					if (!proc.isAlive()) break;
				}
			}
			catch (IOException e) {
				LOG.error("Uncaught exception in OutConsumerThread: " + e.getMessage(), e);
			} catch (InterruptedException e) {}
		}
	}
	
}
