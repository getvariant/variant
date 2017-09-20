package com.variant.core.schema.parser;

import com.variant.core.schema.Flusher;

/**
 * User Hook processor.
 * 
 * @author
 *
 */
public interface FlusherService {

	/**
	 * Initialize a flusher.
	 */
	void initFlusher(Flusher fluhser);
	
	/**
	 * Null Flusher service, which does nothing whatsoever — good enough for core tests.
	 * 
	 */
	public static final FlusherService NULL = new FlusherService() {
		
		@Override
		public void initFlusher(Flusher flusher) {}
		
	};
}
