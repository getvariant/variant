package com.variant.core.lifecycle;



/**
 * Parse time life-cycle event which posts its hooks whenever the schema deployer successfully parses a variation.
 * Will not post for those variations which contained parse errors. Use this hook to enforce application 
 * semantics which are external to the experience variation model (XVM).
 * 
 * @since 0.7
 *
 */
public interface VariationParsedLifecycleEvent extends VariationAwareLifecycleEvent, ParsetimeLifecycleEvent {
	
   /**
    * A {@link com.variant.core.lifecycle.LifecycleHook.PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link VariationParsedLifecycleEvent}.
    * 
    * @since 0.7
    */
   public interface PostResult extends LifecycleHook.PostResult {}

}
