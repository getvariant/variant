package com.variant.core.lifecycle;



/**
 * <p>Parse time life cycle event. Posts its hooks whenever the schema parser successfully completes parsing of a test. 
 * Will not post for a test if parse errors were encountered. Use this hook to enforce application 
 * semantics that is external to XDM.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface TestParsedLifecycleEvent extends TestAwareLifecycleEvent, ParsetimeLifecycleEvent {
	
   /**
    * A {@link com.variant.core.lifecycle.LifecycleHook.PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link TestParsedLifecycleEvent}.
    * 
    * @since 0.7
    */
   public interface PostResult extends LifecycleHook.PostResult {}

}
