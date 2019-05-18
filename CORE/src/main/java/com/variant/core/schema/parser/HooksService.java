package com.variant.core.schema.parser;

import com.variant.core.schema.Hook;

/**
 * Most basic hook service that does nothing in particular.
 * We need to have it in core because we parse the schema here. At run time,
 * This will be overridden by server or client side services.
 *
 */
public class HooksService {

	/**
	 * Add an unbound life-cycle hook to this hooker.
	 */
	public void initHook(Hook hook, ParserResponse parserResponse) {}
	
	/**
	 * Post all hooks for a particular LCE type. This triggers post of all event types
	 * assignable to the passed type, i.e. all of its subtypes.
	 * 
	 * @param event
	 * @return Optional containing the first non-empty post result from the chain, or an empty Optional,
	 *         if none of the posted hooks returned a non-empty Optional.
	 * 
	 *
	public LifecycleEvent.PostResult post(LifecycleEvent event);
	*/
}

