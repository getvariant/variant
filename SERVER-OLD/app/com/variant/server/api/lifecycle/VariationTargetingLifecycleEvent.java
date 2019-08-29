package com.variant.server.api.lifecycle;

import com.variant.core.schema.Variation.Experience;

/**
 * <p>Lifecycle event raised when Variant is about to target a user session for a variation, after the 
 * session has already been qualified for this variation. This event will not be raised for a session
 * which has been disqualified for the variation.
 * 
 * <p>If no hooks for this event are defined in the experiment schema, 
 * Variant server targets the session randomly, according to experience weights provided in the schema.
 * If no experience weights were provided in the schema and no applicable targeting hooks have been
 * defined (or none returned a non-empty result), a run-time user error will be emitted.
 * 
 * <p>If a user hook, subscribed to this event, wishes to target a session to a particular test experience,
 * its <code>post()</code> method must return an object of type {@link PostResult}. An empty post result 
 * object is obtained by calling the {@link #mkPostResult()} factory method.
 * 
 * @since 0.7
 *
 */
public interface VariationTargetingLifecycleEvent extends VariationAwareLifecycleEvent, StateAwareLifecycleEvent {
	
   /**
    * The return type of the <code>Lifecycle&lt;VariationTargetingLifecycleEvent&gt;Hook#post(VariationTargetingLifecycleEvent)</code>
    * method.  An empty object is obtained by calling the {@link #mkPostResult()} factory method.
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
	public PostResult mkPostResult();

}
