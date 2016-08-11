package com.variant.core.hook;

import com.variant.core.schema.ParserResponse;

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
	 * under construction by the currently in-progress invocation of parser. Host code may add 
	 * parser messages to this response by calling 
	 * {@link ParserResponse#addMessage(com.variant.core.schema.ParserMessage.Severity, String)}.
	 * 
	 * @return An object of type {@link com.variant.core.schema.ParserResponse}.
     * @since 0.5
	 */
	public ParserResponse getParserResponse();
}
