package com.variant.core.flashpoint;

import com.variant.core.schema.parser.ParserResponse;

/**
 * <p>Super-interface for all flashpoint types that occur at schema parse time.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface ParserFlashpoint extends Flashpoint {

	/**
	 * Client code may obtain the {@link com.variant.core.schema.parser.ParserResponse} object
	 * under construction by the current invocation of parser.
	 * 
	 * @return An object of type {@link com.variant.core.schema.parser.ParserResponse}.
     * @since 0.5
	 */
	public ParserResponse getParserResponse();
}
