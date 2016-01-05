package com.variant.core.hook;

import com.variant.core.schema.State;

/**
 * <p>Parse time user hook that is reached immediately after a state is successfully parsed.
 * This hook will not be reached if errors were encountered during parsing of a state.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface StateParsedHook extends ParserHook {

	/**
	 * Client code may obtain the state for which this hook was reached.
	 * 
	 * @return An object of type {@link com.variant.core.schema.State}.
     * @since 0.5
	 */
	public State getState();
}
