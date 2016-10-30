package com.variant.server.event;

import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

import org.apache.commons.lang3.time.DurationFormatUtils

import com.variant.core.exception.VariantInternalException
import com.variant.core.exception.VariantRuntimeException
import com.variant.core.exception.VariantRuntimeUserErrorException
import com.variant.core.impl.VariantCore
import com.variant.core.impl.VariantCoreInitParamsImpl
import com.variant.core.xdm.impl.MessageTemplate
import com.variant.server.boot.VariantConfig
import com.variant.server.boot.VariantConfigKey._

import play.api.Logger

class EventWriter (core: VariantCore, config: VariantConfig) {
	
   private val logger = Logger(this.getClass)
   private val bufferSize = config.getInt(EventWriterBufferSize)
   private val pctFullSize = bufferSize * config.getInt(EventWriterPercentFull) / 100
	private val pctEmptySize = Math.ceil(bufferSize * 0.1).intValue()
	private val maxFlusherDelayMillis = config.getLong(EventWriterFlushMaxDelayMillis)

	// Asynchronous flusher thread consumes events from the holding queue.
   private val flusherThread = new FlusherThread();
	// Not a daemon. Intercept interrupt and flush the buffer before exiting.
	flusherThread.setDaemon(false)
	flusherThread.setName("Variant Event Writer")
	flusherThread.start()

	// The underlying buffer is a non-blocking, unbounded queue. We will enforce the soft upper bound,
	// refusing inserts that will put the queue size over the limit, but not worrying about
	// a possible overage due to concurrency.
	private val bufferQueue = new ConcurrentLinkedQueue[VariantFlushableEvent]()
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 *  Instantiate externally defined event flusher.
	 */
	val flusher = {
		val className = config.getString(EventFlusherClassName)
		try {
			val result = {
			   val clazz = Class.forName(className).newInstance();		
   			if (!clazz.isInstanceOf[EventFlusher]) {
   				throw new VariantRuntimeUserErrorException (MessageTemplate.BOOT_EVENT_FLUSHER_NO_INTERFACE, className, classOf[EventFlusher].getName);
	   		}
		  	   clazz.asInstanceOf[EventFlusher]
			}
			
			val json = config.getString(EventFlusherClassInit)
			try {
            result.init(new VariantCoreInitParamsImpl(core, json));
			}
			catch {
			   case _: Throwable =>
               throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON, json, EventFlusherClassInit.name);
			}
			result	
		}
		catch {
		   case e : VariantRuntimeException => throw e
		   case e : Throwable => {
		      logger.error("Unable to instantiate event flusher class [" + className +"]", e)
		      throw new VariantInternalException("Unable to instantiate event flusher class [" + className +"]", e);
		   }
		}
	}

	logger.debug("Event writer started.");

	/**
	 * Shutdown this event writer.
	 * Cannot be used after this.
	 */
	def shutdown() {
	   flusherThread.interrupt()
      logger.debug("Event writer shutdown.");
	}
	
	/**
	 * Write collection of events to the queue.  This method never blocks:
	 * if there's no room on the queue to hold all the events, write as many as we can in the
	 * order of the collection's iterator and ignore the rest. Log ERROR dropped events.
	 *  
	 * @param event decoratedEvent
	 *                   
	 * @return number of elements actually written.
	 */
	def write(event: VariantFlushableEvent) {
				
		// We don't worry about possible concurrent writes because the underlying
		// queue implementation is thread safe and unbound.  It's okay to temporarily 
		// go over the queueSize due to concurrency, so long as we eventually shrink back.
		// But we won't go over it knowingly.
		val currentSize = bufferQueue.size();
		var dropCount = 0
		
		if (currentSize < bufferSize) {
			bufferQueue.add(event);
		}
		else {
		   val msg = "Dropped %d event(s) due to buffer overflow. Consider increasing %d system property (current value %d)"
			logger.trace(msg.format(1, EventWriterBufferSize.name, bufferSize))
			dropCount += 1
			if (dropCount % 1000 == 0)
			   logger.info(msg.format(1000, EventWriterBufferSize.name, bufferSize))
		}
		
		// Block momentarily to wake up the flusher thread if the queue has reached the pctFull size.
		synchronized {
		   if (currentSize >= pctFullSize) bufferQueue.notify();
		}

	}

	/**
    * Flusher thread.
    * Removes events from the queue and flushes them to an event persistence interface. 
    * 
    * @author Igor.
    *
    */
   class FlusherThread extends Thread {
   	
      private val logger = Logger(this.getClass)
   
   	override def run() {
      		
   		var interruptedExceptionThrown = false;
   		var timeToGo = false;
   		while (!timeToGo) {
   			
   			try {
   				// We were either woken up because the queue is over pctFull,
   				// or we timed out waiting. Flush either way, event if under pctEmpty,
   				// and the keep flushing until under pctEmpty.
   				do {
   					flush();
   				} while (bufferQueue.size() >= pctEmptySize);
   									
   				// Block until the queue is over pctFull again, but with timeout.
   				bufferQueue.synchronized {
   					bufferQueue.wait(maxFlusherDelayMillis);
   				}
   			}
   			catch {
   			   case _ : InterruptedException => interruptedExceptionThrown = true
   			   case t : Throwable => logger.error("Ignoring unexpected exception in event writer.", t)
   			}
   			
   			if (interruptedExceptionThrown || isInterrupted()) {
   				try {
   					flush();
   				}
   				catch {
   				   case t : Throwable => logger.error("Unexpected exception in async database event writer.", t);
   				}
   				timeToGo = true
   			}
   		}   		
   	}		
   	
   	/**
   	 * Flush the entire queue to an event flusher.
   	 * Package visibility to let test call this.  Must be synchronized because
   	 * tests may call flush directly concurrently with the regular async path.
   	 * Should be no overhead during regular code path.
   	 */
   	private def flush() {
   
   		var events = new LinkedList[VariantFlushableEvent]();
   		while(!bufferQueue.isEmpty()) events.add(bufferQueue.poll());

   		if (!events.isEmpty()) {
      		var now = System.currentTimeMillis();
   		   flusher.flush(events);		
   		   logger.info("Flushed " + events.size() + " event(s) in " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - now));
   		}
   	}
   }

}
