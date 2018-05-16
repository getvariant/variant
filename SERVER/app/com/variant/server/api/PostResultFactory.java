package com.variant.server.api;

import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook.PostResult;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;
import com.variant.core.lifecycle.TestParsedLifecycleEvent;
import com.variant.server.impl.StateParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.TestParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.TestQualificationLifecycleEventPostResultImpl;
import com.variant.server.impl.TestTargetingLifecycleEventPostResultImpl;
import com.variant.server.lifecycle.TestQualificationLifecycleEvent;
import com.variant.server.lifecycle.TestTargetingLifecycleEvent;

/**
 * Factory class for various concrete implementations of {@link PostResult}.
 * Each concrete user hook gets a chance to run when Variant calls its <code>post()</code> method.
 * The <code>post()</code> method communicates its outcome back to Variant by returning an object 
 * of type {@link PostResult}, corresponding to the concrete life cycle event type.
 * The <code>post()</code> method obtained an instance of its result class by calling one of 
 * the factory methods below.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */

public class PostResultFactory {
   
   private PostResultFactory() {} // No instantiation.

	/**
	 * Obtain an instance of {@link PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
	 * invoked with the event type of {@link StateParsedLifecycleEvent}.
	 * 
	 * @return An "empty" instance of type {@link com.variant.core.schema.StateParsedLifecycleEvent.PostResult}. Use mutator
	 *         method(s) of this object to pass additional state back to server. 
	 * 
    * @since 0.7
	 */
	public static StateParsedLifecycleEvent.PostResult mkPostResult(StateParsedLifecycleEvent event) {
		return new StateParsedLifecycleEventPostResultImpl(event);
	}
	
   /**
    * Obtain an instance of {@link PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link TestParsedLifecycleEvent}.
    * 
    * @return An "empty" instance of type {@link com.variant.core.schema.TestParsedLifecycleEvent.PostResult}. Use mutator
    *         method(s) of this object to pass additional state back to server. 
    * 
    * @since 0.7
    */
	public static TestParsedLifecycleEvent.PostResult mkPostResult( TestParsedLifecycleEvent event) {
		return new TestParsedLifecycleEventPostResultImpl(event);
	}

   /**
    * Obtain an instance of {@link PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link TestQualificationLifecycleEvent}.
    * 
    * @return An "empty" instance of type {@link TestQualificationLifecycleEvent.PostResult}. Use mutator
    *         method(s) of this object to pass additional state back to server. 
    * 
    * @since 0.7
    */
	public static TestQualificationLifecycleEvent.PostResult mkPostResult(TestQualificationLifecycleEvent event) {
		return new TestQualificationLifecycleEventPostResultImpl(event);	
	}

   /**
    * Obtain an instance of {@link PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link TestTargetingLifecycleEvent}.
    * 
    * @return An "empty" instance of type {@link TestTargetingLifecycleEvent.PostResult}. Use mutator
    *         method(s) of this object to pass additional state back to server. 
    * 
    * @since 0.7
    */
	public static TestTargetingLifecycleEvent.PostResult mkPostResult(TestTargetingLifecycleEvent event) {
		return new TestTargetingLifecycleEventPostResultImpl(event);	
	}

}

