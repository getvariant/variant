package com.variant.client.test;

import static org.junit.Assert.fail;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTestWithServerAsync extends ClientBaseTestWithServer {
		
	private final Executor pool = Executors.newFixedThreadPool(4);
	private final AtomicInteger taskCount = new AtomicInteger(0);
	private final ConcurrentLinkedQueue<Throwable> thrown = new ConcurrentLinkedQueue<Throwable>();
	
	/**
	 * @param r
	 */
	protected <T> void async (Runnable block) throws Exception {
		
	      taskCount.addAndGet(1);

	      pool.execute(() -> {
	         try {
		         block.run();
	         } 
	         catch (Throwable t) {
	        	 thrown.add(t);
	         } 
	         finally {
	        	 taskCount.decrementAndGet();
	         }	      	
	      });
	}
	
	   /**
	    * Block for all functions to complete.
	    * TODO: replace with java.util.concurrent.CountDownLatch
	 * @throws InterruptedException 
	    */
	protected void joinAll(long timeout) throws InterruptedException {
		
	      long wated = 0;
	      while (taskCount.get() > 0 && wated < timeout) {
	         Thread.sleep(200);
	         wated += 200;
	      }
	      if (wated >= timeout) 
	    	  fail("Unexpected timeout waiting for background threads.");
	      
	      if (thrown.size() > 0) {
	    	  for (Throwable t: thrown) t.printStackTrace();
	    	  fail("Failing due to previous exceptions in async block(s)");
	      }
	   }

	   /**
	    * Block for all functions to complete.
	    * TODO: replace with java.util.concurrent.CountDownLatch
	 * @throws InterruptedException 
	    */
	   protected void joinAll() throws InterruptedException { 
		   joinAll(20000);
	   }

}

