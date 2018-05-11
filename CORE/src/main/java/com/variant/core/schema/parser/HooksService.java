package com.variant.core.schema.parser;

import com.variant.core.UserHook;
import com.variant.core.lce.LifecycleEvent;
import com.variant.core.schema.Hook;

/**
 * User Hook processor.
 * 
 * @author
 *
 */
public interface HooksService {

	/**
	 * Add an unbound user hook to this hooker.
	 */
	void initHook(Hook hook, ParserResponse parserResponse);
	
	/**
	 * Post all hooks for a particular LCE type. This triggers post of all event types
	 * assignable to the passed type, i.e. all of its subtypes.
	 * 
	 * @param event
	 * @return the event passed in as argument.
	 */
	public UserHook.PostResult post(LifecycleEvent event);

	/**
	 * Null hooker, which does nothing whatsoever.
	 * Good enough for core tests and for the client side (there are no hooks on the client).
	 * 
	 * @author Igor
	 *
	 */
	public static final HooksService NULL = new HooksService() {
		
		@Override
		public void initHook(Hook hook, ParserResponse parserResponse) {}
		
		@Override
		public UserHook.PostResult post(LifecycleEvent hook) {return null;}
	};
}

