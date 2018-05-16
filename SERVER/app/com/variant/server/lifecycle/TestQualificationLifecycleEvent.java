package com.variant.server.lifecycle;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.TestAwareLifecycleEvent;


/**
 * <p>Life cycle event, triggered when Variant session first comes in contact with a live test. 
 * If no user hooks for this event are defined in the experiment schema, Variant server uses
 * the default hook, which blindly qualifies all user sessions for all live tests. If that's
 * the desired behavior, no user hooks are needed.
 * 
 * <p>If a user hook, subscribed to this event, wishes to (dis)qualify the associated test, its
 * <code>post()</code> method must return an object of type {@link PostResult}. An empty post result 
 * object is obtained by calling the {@link PostResultFactory#mkPostResult(TestQualificationLifecycleEvent)} 
 * factory method.
 * 
 * @author Igor Urisman.
 * @see LifecycleHook
 * @see PostResult
 * @since 0.5
 *
 */
public interface TestQualificationLifecycleEvent extends RuntimeLifecycleEvent, TestAwareLifecycleEvent {
      
   /**
    * The return type of the {@link LifecycleHook#post(com.variant.core.LifecycleEvent) LifecycleHook.post(TestQualificationLifecycleEvent)} 
    * method.
    * 
    * @since 0.7
    */
   public interface PostResult extends LifecycleHook.PostResult {
      
      /**
       * Set whether the session is qualified for the associated test.
        *
       * @param qualified 
       * @since 0.7
       */
      public void setQualified(boolean qualified);

      /**
       * <p>The {@link LifecycleHook#post(com.variant.core.LifecycleEvent) LifecycleHook.post(TestQualificationLifecycleEvent)} method
       * may call this to inform Variant server whether the entry for this test should
       * be removed from this session's targeting tracker, in the case it is disqualified.
       * If the associated session was disqualified for the associated test, and this method was passed <code>true</code>, 
       * the entry for this test in the targeting tracker on the client will be discarded. Otherwise, existing entries for
       * disqualified tests are left intact.
       * 
       * <p>Variant server will ignore the value set with this method, if the associated session was not disqualified for
       * the associated test.
       * 
       * @param remove
       * @since 0.7
       */
      public void setRemoveFromTargetingTracker(boolean remove);
   }

}
