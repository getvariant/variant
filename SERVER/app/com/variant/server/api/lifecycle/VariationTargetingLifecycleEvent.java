package com.variant.server.api.lifecycle;

import com.variant.core.schema.Variation.Experience;

/**
 * <p>Life cycle event triggered when Variant is about to target a user session for a test, but after the 
 * session has already been qualified for this test. This event will not be triggered if this session has
 * been disqualified for the test. If no hooks for this event are defined in the experiment schema, 
 * Variant server targets the session randomly, according to experience weights supplied in the schema.
 * If no applicable targeting hooks have been defined and no exeprience weights provided in the schema,
 * a run-time user error will result.
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
public interface VariationTargetingLifecycleEvent extends VariationAwareLifecycleEvent, StateAwareLifecycleEvent {
	
   /**
    * The return type of the {@link LifecycleHook#post(com.variant.core.LifecycleEvent) LifecycleHook.post(TestTargetingLifecycleEvent)} 
    * method.
    * 
    * @since 0.7
    */
	public interface PostResult extends LifecycleEvent.PostResult {
	   
	   /**
	    * Set the test experience for this session in this test.
	    * .
	    * @param experience Targeted experience.
	    * @since 0.7
	    */
	    public void setTargetedExperience(Experience experience);   

	}

	/**
	 * Override the return type with the narrower one, suitable for this concrete lifecycle event.
	 * This avoid unnecessary casts in the client code.
	 * 
	 * @since 0.10
	 */
	@Override
	public PostResult newPostResult();

}
