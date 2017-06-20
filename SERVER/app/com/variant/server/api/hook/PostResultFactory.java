package com.variant.server.api.hook;

import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.core.schema.StateParsedLifecycleEventPostResult;
import com.variant.core.schema.TestParsedLifecycleEvent;
import com.variant.core.schema.TestParsedLifecycleEventPostResult;
import com.variant.server.impl.StateParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.TestParsedLifecycleEventPostResultImpl;
import com.variant.server.impl.TestQualificationLifecycleEventPostResultImpl;
import com.variant.server.impl.TestTargetingLifecycleEventPostResultImpl;

///TODO
public class PostResultFactory {

	/**
	 * 
	 */
	public static StateParsedLifecycleEventPostResult mkPostResult(StateParsedLifecycleEvent event) {
		return new StateParsedLifecycleEventPostResultImpl(event);
	}
	
	/**
	 * 
	 */
	public static TestParsedLifecycleEventPostResult mkPostResult( TestParsedLifecycleEvent event) {
		return new TestParsedLifecycleEventPostResultImpl(event);
	}

	/**
	 * 
	 */
	public static TestQualificationLifecycleEventPostResult mkPostResult(TestQualificationLifecycleEvent event) {
		return new TestQualificationLifecycleEventPostResultImpl(event);	
	}

	/**
	 * 
	 */
	public static TestTargetingLifecycleEventPostResult mkPostResult(TestTargetingLifecycleEvent event) {
		return new TestTargetingLifecycleEventPostResultImpl(event);	
	}

}

