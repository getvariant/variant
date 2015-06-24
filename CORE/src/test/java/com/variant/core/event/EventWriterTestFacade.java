package com.variant.core.event;

public class EventWriterTestFacade {

	private EventWriter eventWriter;
	
	public EventWriterTestFacade(EventWriter eventWriter) {
		this.eventWriter = eventWriter;
	}
	
	/**
	 * In the underlying implementation, this method is package visible.
	 * We need to make it public for tests.
	 * @return
	 */
	public EventPersister getEventPersister() {
		return eventWriter.getEventPersister();
	}
}
