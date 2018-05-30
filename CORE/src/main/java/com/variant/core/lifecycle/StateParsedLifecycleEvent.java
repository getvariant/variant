package com.variant.core.lifecycle;

/**
 * Parse time life-cycle event which posts its hooks whenever the schema parser successfully parses a state.
 * Will not post for those states which contained parse errors. Use this hook to enforce application 
 * semantics that is external to the experience variation model (XVM),
 * e.g. that a certain state parameter, expected by the application, was supplied.
 * 
 * @since 0.7
 *
 */
public interface StateParsedLifecycleEvent extends StateAwareLifecycleEvent, ParsetimeLifecycleEvent {
	
	/**
    * A {@link com.variant.core.lifecycle.LifecycleHook.PostResult} suitable as the return type for {@link LifecycleHook#post(LifecycleEvent)},
    * called with the event type of {@link StateParsedLifecycleEvent}. Client code may add additional parser
    * messages of arbitrary severity via {@link #addMessage(com.variant.core.UserError.Severity, String)}.
    * 
    * @since 0.7
    */
	public interface PostResult extends LifecycleHook.PostResult {}
}