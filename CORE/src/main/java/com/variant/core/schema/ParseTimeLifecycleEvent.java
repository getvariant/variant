package com.variant.core.schema;

import com.variant.core.LifecycleEvent;

/**
 * <p>Super-interface for all life cycle event types that post their hooks at schema parse time.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface ParseTimeLifecycleEvent extends LifecycleEvent {

	/**
	 * Host code may obtain the {@link com.variant.core.schema.ParserResponse} object
	 * under construction by the currently in-progress invocation of parser. 
	 * 
	 * @return An object of type {@link com.variant.core.schema.ParserResponse}.
     * @since 0.5
	 */
	ParserResponse getParserResponse();
	
	/**
     * Add a message to the parser response under construction by the currently 
     * in-progress invocation of parser.
	 * 	
     * @since 0.6
	 */
    void addMessage(String message);

}