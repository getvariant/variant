package com.variant.core.schema;


/**
 * <p>Parse time life cycle event. Posts its hooks whenever the schema parser 
 * successfully completes parsing of a state. Will not post for a state if parse errors were encountered. 
 * Use this life cycle event to enforce application semantics that is external to XDM, e.g. that a certain 
 * state parameter was supplied.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface StateParsedLifecycleEvent extends ParseTimeLifecycleEvent {

	/**
	 * The state for which this hook is posting. It is safe to assume that no errors were
	 * encountered during parsing of this state.
	 * 
	 * @return An object of type {@link com.variant.core.xdm.State}.
     * @since 0.5
	 */
	public State getState();
}
