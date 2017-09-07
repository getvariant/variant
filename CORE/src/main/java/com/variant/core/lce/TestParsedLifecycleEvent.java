package com.variant.core.lce;

import com.variant.core.UserHook;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.Test;


/**
 * <p>Parse time life cycle event. Posts its hooks whenever the schema parser successfully completes parsing of a test. 
 * Will not post for a test if parse errors were encountered. Use this hook to enforce application 
 * semantics that is external to XDM.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface TestParsedLifecycleEvent extends TestAwareLifecycleEvent, ParsetimeLifecycleEvent {
	
   /**
    * A {@link com.variant.core.UserHook.PostResult} suitable as the return type of the {@link UserHook#post(LifecycleEvent)},
    * invoked with the event type of {@link TestParsedLifecycleEvent}.
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
