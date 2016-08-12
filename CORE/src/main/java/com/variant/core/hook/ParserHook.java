package com.variant.core.hook;

import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.ParserMessage.Severity;

/**
 * <p>Super-interface for all user hook types that post their listeners at schema parse time.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface ParserHook extends UserHook {

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
    void addMessage(Severity severity, String message);

}
