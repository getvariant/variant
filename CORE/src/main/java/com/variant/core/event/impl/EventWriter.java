package com.variant.core.event.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.config.VariantProperties;
import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEventDecorator;

public class EventWriter {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(EventWriter.class);

	// The underlying buffer is a non-blocking, unbounded queue. We will enforce the soft upper bound,
	// refusing inserts that will put the queue size over the limit, but not worrying about
	// a possible overage due to concurrency.
	private ConcurrentLinkedQueue<VariantEventDecorator> eventQueue = null;

	// Max queue size (soft).
	private int queueSize;
	
	// Number of entries in the queue that will trigger the wakeup of the persister thread.
	private int pctFullSize;
	
	// Number of entries in the queue that we won't attempt to get under.
	private int pctEmptySize;
	
	// The persister thread will wake up at least this frequently and flush the queue.
	private long maxPersisterDelayMillis;
		
	// Asynchronous persister thread consumes events from the holding queue.
	private PersisterThread persisterThread;
	
	// The actual event persister passed to the constructor by client code.
	private EventPersister persisterImpl = null;
			
	/**
	 * Expose event persister to tests via package visibility.
	 * @return
	 */
	public EventPersister getEventPersister() {
		return persisterImpl;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * Constructor
	 */
	public EventWriter(EventPersister persisterImpl) {
		
		this.persisterImpl = persisterImpl;
		this.queueSize = VariantProperties.getInstance().eventWriterBufferSize();
		this.pctFullSize = queueSize * VariantProperties.getInstance().eventWriterPercentFull() / 100;
		this.pctEmptySize = (int) Math.ceil(queueSize * 0.1);
		this.maxPersisterDelayMillis = VariantProperties.getInstance().eventWriterMaxDelayMillis();
		
		eventQueue = new ConcurrentLinkedQueue<VariantEventDecorator>();
		
		persisterThread = new PersisterThread();
		
		// Not a daemon: intercept interrupt and flush the buffer before exiting.
		persisterThread.setDaemon(false);
		persisterThread.setName(PersisterThread.class.getSimpleName());
		persisterThread.start();
	}
		
	/**
	 * Shutdown this event writer.
	 * Cannot be used after this.
	 */
	public void shutdown() {
		long now = System.currentTimeMillis();
		persisterThread.interrupt();
		persisterThread = null;
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
	 * @param eventsPair A pair of 1) a collection of events, and 2) a collection of test experiences
	 *                   that were all in effect when these events were triggered.
	 *                   
	 * @return number of elements actually written.
	 */
	public void write(Collection<VariantEventDecorator> events) {
				
		// size() is an O(n) operation - do it once.
		// We don't worry about possible concurrent writes because the underlying
		// queue implementation is unbound.  It's okay to temporarily go over the
		// queueSize due to concurrency, so long as we eventually shrink back.
		int currentQueueSize = eventQueue.size();
		int delta = events.size();
		
		Iterator<VariantEventDecorator> iter = events.iterator();
		while (currentQueueSize < queueSize && iter.hasNext()) {
			VariantEventDecorator event = iter.next();
			eventQueue.add(event);
			currentQueueSize++;
			delta--;
		}

		if (delta > 0) {
			System.out.println(currentQueueSize);
			LOG.error(
					"Memory buffer is full. Dropped [" + delta + 
					"] events. Consider increasing " + VariantProperties.Keys.EVENT_WRITER_BUFFER_SIZE.propName() + 
					" system property (current value [" + queueSize + "])");
		}
		
		// Block momentarily to wake up the persister thread if the queue has reached the pctFull size.
		synchronized (eventQueue) {
			if (currentQueueSize >= pctFullSize) eventQueue.notify();
		}

	}
			
	/**
	 * Persister thread.
	 * Removes events from the queue and flushes them to an event persistence interface. 
	 * 
	 * @author Igor.
	 *
	 */
	private class PersisterThread extends Thread {
		
		@Override
		public void run() {

			LOG.debug("Event persister thread " + Thread.currentThread().getName() + " started.");
			
			boolean InterruptedExceptionThrown = false;
			
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
						eventQueue.wait(maxPersisterDelayMillis);
					}

				}
				catch (InterruptedException e) {
					InterruptedExceptionThrown = true;
				}
				catch (Throwable t) {
					LOG.error("Unexpected exception in async database event writer.", t);
				}
				
				if (InterruptedExceptionThrown || Thread.currentThread().isInterrupted()) {
					try {
						flush();
					}
					catch (Throwable t) {
						LOG.error("Unexpected exception in async database event writer.", t);
					}
					if (LOG.isDebugEnabled()) {
						LOG.debug("Event persister thread " + Thread.currentThread().getName() + " interrupted and exited.");
					}
					return;
				};
			}
			
		}		
		
		/**
		 * Flush the entire queue to an event persister.
		 * Package visibility to let test call this.  Must be synchronized because
		 * tests may call flush directly concurrently with the regular async path.
		 * Should be no overhead during regular code path.
		 */
		private void flush() throws Exception {

			LinkedList<VariantEventDecorator> events = new LinkedList<VariantEventDecorator>();

			VariantEventDecorator event;
			while ((event = eventQueue.poll()) != null) events.add(event);

			if (events.isEmpty()) return;
			
			long now = System.currentTimeMillis();
			persisterImpl.persist(events);		
			LOG.debug("Wrote " + events.size() + " events in " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - now));
		}
	}
}
