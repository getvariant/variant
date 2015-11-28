package com.variant.core.flashpoint;

import com.variant.core.schema.State;

/**
 * <p>Parse time flashpoint that is reached immediately after a state is successfully parsed.
 * This flashpoint will not be reached if errors were encountered during parsing of a state.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface StateParsedFlashpoint extends ParserFlashpoint {

	/**
	 * Client code may obtain the state for which this flashpoint was reached.
	 * 
	 * @return An object of type {@link com.variant.core.schema.State}.
     * @since 0.5
	 */
	public State getState();
}
