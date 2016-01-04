package com.variant.core.hook;

import com.variant.core.schema.parser.ParserResponse;

/**
 * <p>Super-interface for all user hook types that occur at schema parse time.
 * 
 * @author Igor.
 * @since 0.5.1
 *
 */
public interface ParserHook extends UserHook {

	/**
	 * Client code may obtain the {@link com.variant.core.schema.parser.ParserResponse} object
	 * under construction by the current invocation of parser.
	 * 
	 * @return An object of type {@link com.variant.core.schema.parser.ParserResponse}.
     * @since 0.5.1
	 */
	public ParserResponse getParserResponse();
}
