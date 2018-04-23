package com.variant.client.test;

import static org.junit.Assert.fail;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ClientBaseTestWithServerAsync extends ClientBaseTestWithServer {
		
	private final Executor pool = Executors.newFixedThreadPool(4);
	private final AtomicInteger taskCount = new AtomicInteger(0);
	private final Throwable[] unexpectedException = {null};
	
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
	        	 unexpectedException[0] = t;
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
	      
	      if (unexpectedException[0] != null) {
	    	  Throwable ue = unexpectedException[0];
	         unexpectedException[0] = null;
	         throw new RuntimeException("Async block crashed: " + ue.getMessage(), ue);
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

