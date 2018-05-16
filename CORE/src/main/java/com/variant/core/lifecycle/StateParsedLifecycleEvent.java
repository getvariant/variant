package com.variant.core.lifecycle;



/**
 * <p>Parse time life cycle event. Posts its hooks whenever the schema parser 
 * successfully completes parsing of a state. Will not post for a state if parse errors were encountered. 
 * Use this life cycle event to enforce application semantics that is external to experiment schema, 
 * e.g. that a certain state parameter, expected by the application, was supplied.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface StateParsedLifecycleEvent extends StateAwareLifecycleEvent, ParsetimeLifecycleEvent {
	
	/**
    * A {@link com.variant.core.lifecycle.LifecycleHook.PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link StateParsedLifecycleEvent}.  No special methods. Client code may add parser
    * messages by via {@link ParsetimeLifecycleEvent#getParserResponse()#}
    * 
    * @since 0.7
    */
	public interface PostResult extends LifecycleHook.PostResult {}
}
