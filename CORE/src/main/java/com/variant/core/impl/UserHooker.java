package com.variant.core.impl;

import com.variant.core.LifecycleEvent;
import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.ParserResponseImpl;

/**
 * User Hook processor.
 * 
 * @author
 *
 */
public interface UserHooker {

	/**
	 * Add an unbound user hook to this hooker.
	 */
	void initHook(Hook hook, ParserResponseImpl parserResponse);
	
	/**
	 * Add a test-bound user hook to this hooker.
	 *
	void addHook(UserHook<? extends LifecycleEvent> hook, Test test);

	/**
	 * Remove all user hooks. 
	 *
	void clear();

	/**
	 * Post all hooks for a particular LCE type. This triggers post of all event types
	 * assignable to the passed type, i.e. all of its subtypes.
	 * 
	 * @param event
	 * @return the event passed in as argument.
	 */
	public LifecycleEvent post(LifecycleEvent event);

	/**
	 * Null hooker, which does nothing whatsoever.
	 * Good enough for core tests and for the client side (there are no hooks on the client).
	 * 
	 * @author Igor
	 *
	 */
	public static final UserHooker NULL = new UserHooker() {
		
		@Override
		public void initHook(Hook hook, ParserResponseImpl parserResponse) {}
		
		//@Override
		//public void addHook(UserHook<? extends LifecycleEvent> hook, Test test) {}

		//@Override
		//public void clear() {}

		@Override
		public LifecycleEvent post(LifecycleEvent hook) {return hook;}
	};
}

