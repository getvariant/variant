package com.variant.core.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.conf.VariantProperties;

public class EventWriter {
	
	// The underlying buffer is a non-blocking, unbounded queue. We will enforce the soft upper bound,
	// refusing inserts that will put the queue size over the limit, but not worrying about
	// a possible overage due to concurrency.
	private ConcurrentLinkedQueue<VariantEventSupport> eventQueue = null;

	// Max queue size (soft).
	private int queueSize;
	
	// Number of entries in the queue that will trigger the wakeup of the persister thread.
	private int pctFullSize;
	
	// Number of entries in the queue that we won't attempt to get under.
	private int pctEmptySize;
	
	// The persister thread will wake up at least this frequently and flush the queue.
	private long maxPersisterIntervalMillis;
		
	// Asynchronous persister thread consumes events from the holding queue.
	private Thread persisterThread;
	
	// The actual event persister passed to the constructor by client code.
	private EventPersister persisterImpl = null;
	
	/**
	 * Validate that event is well-formed.
	 * @param event
	 */
	private void validateEvent(VariantEventSupport event) {
		if (event.experiences.size() == 0) 
			throw new VariantInternalException(
					String.format("Event [%s] [%s] has no experiences. Ignored.", event.eventName, event.eventValue));
	}
	
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
	 * Event writer configurator.
	 * @author Igor.
	 *
	 */
	public static class Config {
		
		// Defaults
		private int bufferSize = VariantProperties.getEventWriterBufferSize();
		private int pctFull = VariantProperties.getEventWriterPercentFull();
		private long maxPersisterIntervalMillis = VariantProperties.getEventWriterMaxDelayMillis();
		
		public Config() {}
		
		public void setBufferSize(int size) {
			this.bufferSize = size;
		}

		public void setPctFull(int pctFull) {
			this.pctFull = pctFull;
		}

		public void setMaxPersisterIntervalMillis(long maxPersisterIntervalMillis) {
			this.maxPersisterIntervalMillis = maxPersisterIntervalMillis;
		}

	}
	
	/**
	 * Constructor
	 */
	public EventWriter(Config config, EventPersister persisterImpl) {
		
		this.persisterImpl = persisterImpl;
		this.queueSize = config.bufferSize;
		this.pctFullSize = config.bufferSize * config.pctFull / 100;
		this.pctEmptySize = (int) Math.ceil(config.bufferSize * 0.1);
		this.maxPersisterIntervalMillis = config.maxPersisterIntervalMillis;
		
		eventQueue = new ConcurrentLinkedQueue<VariantEventSupport>();
		
		persisterThread = new Thread(new PersisterThread());
		
		// Not a daemon: intercept interrupt and flush the buffer before exiting.
		persisterThread.setDaemon(false);
		persisterThread.setName(PersisterThread.class.getSimpleName());
		persisterThread.start();
	}
		
	/**
	 * Write collection of events to the queue.  This method never blocks:
	 * if there's no room on the queue to hold all the events, write as many as we can in the
	 * order of the collection's iterator and ignore the rest, returning back to the caller 
	 * the number of elements that were accepted. The caller may re-attempt, drop, log, etc.
	 *  
	 * @param events
	 * @return number of elements actually written.
	 */
	public int write(Collection<VariantEventSupport> events) {
		
		// size() is an O(n) operation - do it once.
		int currentQueueSize = eventQueue.size();
		int acceptCount = 0;
		
		Iterator<VariantEventSupport> iter = events.iterator();
		while (currentQueueSize < queueSize && iter.hasNext()) {
			VariantEventSupport event = iter.next();
			validateEvent(event);
			eventQueue.add(event);
			currentQueueSize++;
			acceptCount++;
		}
		
		// Block momentarily to wake up the persister thread if the queue has reached the pctFull size.
		synchronized (eventQueue) {
			if (currentQueueSize >= pctFullSize) eventQueue.notify();
		}

		return acceptCount;
	}
		
	/**
	 * Enqueue a single event.  Variation of the above.
	 *  
	 * @param event
	 */
	public void write(VariantEventSupport event) {
		ArrayList<VariantEventSupport> collection = new ArrayList<VariantEventSupport>(1);
		collection.add(event);
		write(collection);
	}
	
	/**
	 * Persister thread.
	 * Removes events from the queue and flushes them to an event persistence interface. 
	 * 
	 * @author Igor.
	 *
	 */
	private class PersisterThread implements Runnable {
		
		@Override
		public void run() {

			Variant.getLogger().debug("Event persister thread " + Thread.currentThread().getName() + " created.");
			
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
						eventQueue.wait(maxPersisterIntervalMillis);
					}

				}
				catch (InterruptedException e) {
					InterruptedExceptionThrown = true;
				}
				catch (Throwable t) {
					Variant.getLogger().error("Unexpected exception in async database event writer.", t);
				}
				
				if (InterruptedExceptionThrown || Thread.currentThread().isInterrupted()) {
					try {
						flush();
					}
					catch (Throwable t) {
						Variant.getLogger().error("Unexpected exception in async database event writer.", t);
					}
					Variant.getLogger().debug("Writer thread " + Thread.currentThread().getName() + " interrupted and exited.");
					return;
				};
			}
			
		}		
		
		/**
		 * Flush the entire queue to an event persister.
		 */
		private void flush() throws Exception {

			ArrayList<VariantEventSupport> events = new ArrayList<VariantEventSupport>();

			VariantEventSupport event = eventQueue.poll();
			while (event != null) {
				events.add(event);
				event = eventQueue.poll();
			}

			if (events.isEmpty()) return;
			
			long now = System.currentTimeMillis();
			persisterImpl.persist(events);		
			Variant.getLogger().debug("Wrote " + events.size() + " events in " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - now));
		}
	}
}
