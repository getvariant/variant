package com.variant.core.event.impl;

import static com.variant.core.VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE;
import static com.variant.core.VariantCorePropertyKeys.EVENT_WRITER_MAX_DELAY_MILLIS;
import static com.variant.core.VariantCorePropertyKeys.EVENT_WRITER_PERCENT_FULL;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantProperties;
import com.variant.core.event.EventFlusher;
import com.variant.core.event.VariantFlushableEvent;

public class EventWriter {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(EventWriter.class);

	// The underlying buffer is a non-blocking, unbounded queue. We will enforce the soft upper bound,
	// refusing inserts that will put the queue size over the limit, but not worrying about
	// a possible overage due to concurrency.
	private ConcurrentLinkedQueue<VariantFlushableEvent> eventQueue = null;

	// Max queue size (soft).
	private int queueSize;
	
	// Number of entries in the queue that will trigger the wakeup of the flusher thread.
	private int pctFullSize;
	
	// Number of entries in the queue that we won't attempt to get under.
	private int pctEmptySize;
	
	// The flusher thread will wake up at least this frequently and flush the queue.
	private long maxFlusherDelayMillis;
		
	// Asynchronous flusher thread consumes events from the holding queue.
	private FlusherThread flusherThread;
	
	// The actual event flusher passed to the constructor by client code.
	private EventFlusher flusherImpl = null;
			
	/**
	 * Expose event flusher to tests via package visibility.
	 * @return
	 */
	public EventFlusher getEventFlusher() {
		return flusherImpl;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * Constructor
	 */
	public EventWriter(EventFlusher flusherImpl, VariantProperties properties) {
		
		this.flusherImpl = flusherImpl;
		this.queueSize = properties.get(EVENT_WRITER_BUFFER_SIZE, Integer.class);
		this.pctFullSize = queueSize * properties.get(EVENT_WRITER_PERCENT_FULL, Integer.class) / 100;
		this.pctEmptySize = (int) Math.ceil(queueSize * 0.1);
		this.maxFlusherDelayMillis = properties.get(EVENT_WRITER_MAX_DELAY_MILLIS, Integer.class);
		
		eventQueue = new ConcurrentLinkedQueue<VariantFlushableEvent>();
		
		flusherThread = new FlusherThread();
		
		// Not a daemon: intercept interrupt and flush the buffer before exiting.
		flusherThread.setDaemon(false);
		flusherThread.setName(flusherThread.getClass().getSimpleName());
		flusherThread.start();
	}
		
	/**
	 * Shutdown this event writer.
	 * Cannot be used after this.
	 */
	public void shutdown() {
		long now = System.currentTimeMillis();
		flusherThread.interrupt();
		flusherThread = null;
		if (LOG.isDebugEnabled()) {
			LOG.debug(
					"Event Writer shutdown in " + (DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
		}
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
	public void write(VariantFlushableEvent event) {
				
		// We don't worry about possible concurrent writes because the underlying
		// queue implementation is thread safe and unbound.  It's okay to temporarily 
		// go over the queueSize due to concurrency, so long as we eventually shrink back.
		// But we won't go over it knowingly.
		int currentSize = eventQueue.size();
		
		if (currentSize < queueSize) {
			eventQueue.add(event);
		}
		else {
			LOG.error(
					"Dropped event due to full memory buffer. Consider increasing " + EVENT_WRITER_BUFFER_SIZE.propertyName() + 
					" system property (current value [" + queueSize + "])");
		}
		
		// Block momentarily to wake up the flusher thread if the queue has reached the pctFull size.
		synchronized (eventQueue) {
			if (currentSize >= pctFullSize) eventQueue.notify();
		}

	}
	
	/**
	 * Flusher thread.
	 * Removes events from the queue and flushes them to an event persistence interface. 
	 * 
	 * @author Igor.
	 *
	 */
	private class FlusherThread extends Thread {
		
		@Override
		public void run() {

			if (LOG.isDebugEnabled()) LOG.debug("Event flusher thread " + Thread.currentThread().getName() + " started.");
			
			boolean interruptedExceptionThrown = false;
			
			while (true) {
				
				try {
					// We were either woken up because the queue is over pctFull,
					// or we timed out waiting. Flush either way, event if under pctEmpty,
					// and the keep flushing until under pctEmpty.
					do {
						flush();
					} while (eventQueue.size() >= pctEmptySize);
										
					// Block until the queue is over pctFull again, but with timeout.
					synchronized (eventQueue) {
						eventQueue.wait(maxFlusherDelayMillis);
					}

				}
				catch (InterruptedException e) {
					interruptedExceptionThrown = true;
				}
				catch (Throwable t) {
					LOG.error("Unexpected exception in async database event writer.", t);
				}
				
				if (interruptedExceptionThrown || isInterrupted()) {
					try {
						flush();
					}
					catch (Throwable t) {
						LOG.error("Unexpected exception in async database event writer.", t);
					}
					if (LOG.isDebugEnabled())
						LOG.debug("Event flusher thread " + Thread.currentThread().getName() + " interrupted and exited.");
					return;
				};
			}
			
		}		
		
		/**
		 * Flush the entire queue to an event flusher.
		 * Package visibility to let test call this.  Must be synchronized because
		 * tests may call flush directly concurrently with the regular async path.
		 * Should be no overhead during regular code path.
		 */
		private void flush() throws Exception {

			LinkedList<VariantFlushableEvent> events = new LinkedList<VariantFlushableEvent>();

			VariantFlushableEvent event;
			while ((event = eventQueue.poll()) != null) events.add(event);

			if (events.isEmpty()) return;
			
			long now = System.currentTimeMillis();
			flusherImpl.flush(events);		
			LOG.info("Flushed " + events.size() + " event(s) in " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - now));
		}
	}
}
