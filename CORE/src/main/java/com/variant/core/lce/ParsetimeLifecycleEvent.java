package com.variant.core.lce;

import com.variant.core.schema.ParserResponse;


/**
 * <p>Super-interface for all life cycle event types that post their hooks at schema parse time.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface ParsetimeLifecycleEvent extends LifecycleEvent {
	
	/**
	 * 
	 * @return Object of type {@link ParserResponse}
	 */
	ParserResponse getParserResponse();
}
