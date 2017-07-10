package com.variant.server.api.hook;

import com.variant.core.UserHook;


/**
 * <p>Life cycle event triggered when Variant is about to target a user session for a test, but after the 
 * session has already been qualified for this test. If no hooks for this event are defined, Variant server 
 * treats all user sessions as qualifying for all tests. 
 * 
 * <p>A user hook may explicitly (dis)qualify the session by calling the setQualified() method. The application 
 * programmer can also control targeting stability for those users who are disqualified. But default, 
 * disqualification does not affect experiment scoped targeting stability . However, application developer 
 * may direct the server to remove the entry for this test from this user's targeting tracker by calling 
 * {@link #setQualified(boolean)}.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface TestQualificationLifecycleEvent extends StateRequestAwareLifecycleEvent, TestScopedLifecycleEvent {
   
   
   ///TODO
   public interface PostResult extends UserHook.PostResult {
      
      /**
       * Host code calls this to inform Variant server whether the current session
       * (as returned by {@link #getSession()}) is qualified for the test (as returned by {@link #getTest()}).
        *
       * @param qualified Whether or not the session qualifies for the test.
       * @since 0.7
       */
      public void setQualified(boolean qualified);

      /**
       * Host code calls this to inform Variant server whether the entry for this test should
       * be removed from this session's targeting tracker, iff it is disqualified. The server
       * will ignore the value set with this method, if this test was not disqualified. Conversely,
       * if this test was disqualified, but this method was never called, the default value is false,
       * which is to say that targeting tracker entries for disqualified tests are not discarded,
       * unless provided by this method.
       * 
       * @param remove
       * @since 0.7
       */
      public void setRemoveFromTargetingTracker(boolean remove);
   }

}
