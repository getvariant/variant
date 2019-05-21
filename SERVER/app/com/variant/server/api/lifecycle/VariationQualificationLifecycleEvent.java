package com.variant.server.api.lifecycle;


/**
 * <p>Lifecycle event, raised when a user session's qualification for a particular variation must be established. 
 * If no user hooks for this event are defined in the experiment schema, Variant server uses
 * the default hook, which qualifies all user sessions for all variations.
 * <p>
 * If a user hook, registered subscribed for event, wishes to (dis)qualify the associated test, its
 * <code>post()</code> method must return an object of type {@link PostResult} with the qualification outcome
 * set via the {@link PostResult#setQualified(boolean)} method. An empty post result 
 * object is obtained by calling {@link #mkPostResult()} factory method of this class.
 * 
 * @since 0.7
 *
 */
public interface VariationQualificationLifecycleEvent extends VariationAwareLifecycleEvent {
      
   /**
    * The return type of the <code>Lifecycle&lt;VariationQualificationLifecycleEvent&gt;Hook#post(VariationQualificationLifecycleEvent)</code>
    * method. An empty object is obtained by calling the {@link #mkPostResult()} factory method.
    * 
    * @since 0.7
    */
   public interface PostResult extends LifecycleEvent.PostResult {
      
      /**
       * Set whether the session is qualified for the associated test.
        *
       * @since 0.7
       */
      public void setQualified(boolean qualified);

   }

	/**
	 * Override the return type with the narrower one, suitable for this class.
	 * This avoids unnecessary casts in the client code.
	 * 
	 * @since 0.10
	 */
	@Override
	public PostResult mkPostResult();

}
