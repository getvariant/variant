package com.variant.core.lce;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.State;
import com.variant.core.UserHook;


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
public interface StateParsedLifecycleEvent extends ParsetimeLifecycleEvent {

	/**
	 * The state for which this hook is posting. It is safe to assume that no errors were
	 * encountered during parsing of this state.
	 * 
	 * @return An object of type {@link com.variant.core.xdm.State}.
    * @since 0.5
	 */
	public State getState();
	
	/**
    * A {@link com.variant.core.UserHook.PostResult} suitable as the return type of the {@link UserHook#post(LifecycleEvent)},
    * invoked with the event type of {@link StateParsedLifecycleEvent}.
    * 
    * @since 0.7
    */
	public interface PostResult extends UserHook.PostResult {
	   
	   /**
	    * Add a custom schema parser message.
	    * 
	    * @since 0.7
	    */
	    public void addMessage(Severity severity, String message);

	}
}
