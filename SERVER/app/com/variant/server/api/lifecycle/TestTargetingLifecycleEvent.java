package com.variant.server.api.lifecycle;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.StateAwareLifecycleEvent;
import com.variant.core.lifecycle.TestAwareLifecycleEvent;
import com.variant.core.schema.Test.Experience;

/**
 * <p>Life cycle event triggered when Variant is about to target a user session for a test, but after the 
 * session has already been qualified for this test. This event will not be triggered for a session and for
 * a test for which it has been disqualified. If no hooks for this event are defined in the experiment schema, 
 * Variant server targets the session randomly, according to experience weights supplied in the schema. 
 * 
  * <p>If a user hook, subscribed to this event, wishes to target a session to a particular test experience,
  * its <code>post()</code> method must return an object of type {@link PostResult}. An empty post result 
 * object is obtained by calling the {@link PostResultFactory#mkPostResult(TestTargetingLifecycleEvent)} 
 * factory method.
 * 
 * <p><b>It is important to understand that if custom targeting decision is not pseudo-random, then the 
 * outcome of such experiment may not be statistically sound: there might be some
 * reason other than the difference in treatment, that explains the difference in measurement.</b>
 * 
 * @author Igor.
 * @since 0.7
 *
 */
public interface TestTargetingLifecycleEvent extends ServerLifecycleEvent, TestAwareLifecycleEvent, StateAwareLifecycleEvent {
	
   /**
    * The return type of the {@link LifecycleHook#post(com.variant.core.LifecycleEvent) LifecycleHook.post(TestTargetingLifecycleEvent)} 
    * method.
    * 
    * @since 0.7
    */
	public interface PostResult extends LifecycleHook.PostResult {
	   
	   /**
	    * Set the test experience for this session in this test.
	    * .
	    * @param experience Targeted experience.
	    * @since 0.7
	    */
	   public void setTargetedExperience(Experience experience);   

	}

}
