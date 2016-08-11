package com.variant.core.hook;

import com.variant.core.xdm.State;

/**
 * <p>Parse time user hook that posts its listeners immediately after a state is successfully parsed.
 * This hook will not post for a state if errors were encountered during parsing of that state.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface StateParsedHook extends ParserHook {

	/**
	 * The state for which this hook is posting. It is safe to assume that no errors were
	 * encountered during parsing of this state.
	 * 
	 * @return An object of type {@link com.variant.core.xdm.State}.
     * @since 0.5
	 */
	public State getState();
}
