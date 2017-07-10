package com.variant.server.api.hook;

import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.core.schema.TestParsedLifecycleEvent;
import com.variant.server.impl.StateParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.TestParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.TestQualificationLifecycleEventPostResultImpl;
import com.variant.server.impl.TestTargetingLifecycleEventPostResultImpl;

///TODO
public class PostResultFactory {

	/**
	 * 
	 */
	public static StateParsedLifecycleEvent.PostResult mkPostResult(StateParsedLifecycleEvent event) {
		return new StateParsedLifecycleEventPostResultImpl(event);
	}
	
	/**
	 * 
	 */
	public static TestParsedLifecycleEvent.PostResult mkPostResult( TestParsedLifecycleEvent event) {
		return new TestParsedLifecycleEventPostResultImpl(event);
	}

	/**
	 * 
	 */
	public static TestQualificationLifecycleEvent.PostResult mkPostResult(TestQualificationLifecycleEvent event) {
		return new TestQualificationLifecycleEventPostResultImpl(event);	
	}

	/**
	 * 
	 */
	public static TestTargetingLifecycleEvent.PostResult mkPostResult(TestTargetingLifecycleEvent event) {
		return new TestTargetingLifecycleEventPostResultImpl(event);	
	}

}

