package com.variant.server.api.lifecycle;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.VariationAwareLifecycleEvent;


/**
 * <p>Life cycle event, triggered when Variant session'a qualification for a particular test must be established. 
 * If no user hooks for this event are defined in the experiment schema, Variant server uses
 * the default hook, which blindly qualifies all user sessions for all live tests.
 * <p>
 * If a user hook, registered to this event, wishes to (dis)qualify the associated test, its
 * <code>post()</code> method must return an object of type {@link PostResult} with the qualification outcome
 * set via the {@link PostResult#setQualified(boolean)} method. An empty post result 
 * object is obtained by calling the {@link PostResultFactory#mkPostResult(TestQualificationLifecycleEvent)} 
 * factory method.
 * 
 * @see LifecycleHook
 * @see PostResult
 * @since 0.7
 *
 */
public interface VariationQualificationLifecycleEvent extends RuntimeLifecycleEvent, VariationAwareLifecycleEvent {
      
   /**
    * The return type of the {@link LifecycleHook#post(com.variant.core.LifecycleEvent) LifecycleHook.post(TestQualificationLifecycleEvent)} 
    * method. An empty post result object is obtained by calling the 
    * {@link PostResultFactory#mkPostResult(TestQualificationLifecycleEvent)} factory method.
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
       * <p>Variant server will ignore the value set by this method if #{@link PostResult#setQualified(boolean)} was not called with <code>false</code>.
       * 
       * @param remove 
       * @since 0.7
       */
      public void setRemoveFromTargetingTracker(boolean remove);
   }

}
