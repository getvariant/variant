package com.variant.core.schema;

import com.variant.core.LifecycleEvent;
import com.variant.core.UserError.Severity;

/**
 * <p>Super-interface for all life cycle event types that post their hooks at schema parse time.
 * 
 * @author Igor Urisman.
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
	 * @param severity The severity of the message. {@code Severity.EROR} and higher will
	 *        render the parsing unsuccessful and prevent the schema deployment.
	 * @param message The message text.
	 * 
     * @since 0.6
	 */
    void addMessage(Severity severity, String message);

}
