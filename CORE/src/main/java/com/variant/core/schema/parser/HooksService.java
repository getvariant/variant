package com.variant.core.schema.parser;

import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.schema.Hook;

/**
 * Life-cycle hooks service.
 * 
 * @author
 *
 */
public interface HooksService {

	/**
	 * Add an unbound life-cycle hook to this hooker.
	 */
	void initHook(Hook hook, ParserResponse parserResponse);
	
	/**
	 * Post all hooks for a particular LCE type. This triggers post of all event types
	 * assignable to the passed type, i.e. all of its subtypes.
	 * 
	 * @param event
	 * @return Optional containing the first non-empty post result from the chain, or an empty Optional,
	 *         if none of the posted hooks returned a non-empty Optional.
	 * 
	 */
	public LifecycleEvent.PostResult post(LifecycleEvent event);

	/**
	 * Null hooker, which does nothing whatsoever.
	 * Good enough for core tests and for the client side parsing (there are no hooks on the client).
	 * 
	 * @author Igor
	 *
	 */
	public static final HooksService NULL = new HooksService() {
		
		@Override
		public void initHook(Hook hook, ParserResponse parserResponse) {}
		
		@Override
		public LifecycleEvent.PostResult post(LifecycleEvent hook) {return null;}
	};
}

