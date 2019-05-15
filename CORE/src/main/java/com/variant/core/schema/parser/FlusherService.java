package com.variant.core.schema.parser;

import java.util.Optional;

import com.variant.core.schema.Flusher;

/**
 * Flusher Service.
 * 
 * @author
 *
 */
public interface FlusherService {

	/**
	 * Initialize a given flusher.
	 * Implementation will use the system wide default, if param is empty.
	 */
	void initFlusher(Optional<Flusher> fluhser);
	
	/**
	 * Null Flusher service, which does nothing whatsoever — good enough for core tests.
	 * 
	 */
	public static final FlusherService NULL = new FlusherService() {
		
		@Override
		public void initFlusher(Optional<Flusher> flusher) {}
		
	};
}
