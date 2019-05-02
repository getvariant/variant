package com.variant.server.api.lifecycle;
/*
import com.variant.core.impl.StateParsedLifecycleEventPostResultImpl;
import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook.PostResult;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;
import com.variant.core.lifecycle.VariationParsedLifecycleEvent;
import com.variant.server.impl.VariationParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.VariationQualificationLifecycleEventPostResultImpl;
import com.variant.server.impl.VariationTargetingLifecycleEventPostResultImpl;

/**
 * A class with a number of static factory methods, returning concrete implementations of {@link PostResult}s,
 * depending on the life-cycle event.
 * Each concrete user hook gets a chance to run when Variant calls its <code>post()</code> method.
 * The <code>post()</code> method communicates its outcome back to Variant by returning an object 
 * of type {@link PostResult}, corresponding to the concrete life cycle event type.
 * The <code>post()</code> method obtained an instance of its result class by calling one of 
 * the factory methods below.
 * 
 * @since 0.7
 *
 *

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
	 *
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
    *
	public static VariationParsedLifecycleEvent.PostResult mkPostResult(VariationParsedLifecycleEvent event) {
		return new VariationParsedLifecycleEventPostResultImpl(event);
	}

   /**
    * Obtain an instance of {@link PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link VariationQualificationLifecycleEvent}.
    * 
    * @return An "empty" instance of type {@link VariationQualificationLifecycleEvent.PostResult}. Use mutator
    *         method(s) of this object to pass additional state back to server. 
    * 
    * @since 0.7
    *
	public static VariationQualificationLifecycleEvent.PostResult mkPostResult(VariationQualificationLifecycleEvent event) {
		return new VariationQualificationLifecycleEventPostResultImpl(event);
	}

   /**
    * Obtain an instance of {@link PostResult} suitable as the return type of the {@link LifecycleHook#post(LifecycleEvent)},
    * invoked with the event type of {@link VariationTargetingLifecycleEvent}.
    * 
    * @return An "empty" instance of type {@link VariationTargetingLifecycleEvent.PostResult}. Use mutator
    *         method(s) of this object to pass additional state back to server. 
    * 
    * @since 0.7
    *
	public static VariationTargetingLifecycleEvent.PostResult mkPostResult(VariationTargetingLifecycleEvent event) {
		return new VariationTargetingLifecycleEventPostResultImpl(event);	
	}

}
*/
