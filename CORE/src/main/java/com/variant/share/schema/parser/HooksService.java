package com.variant.share.schema.parser;

import com.variant.share.schema.Hook;

/**
 * Most basic hook service that does nothing in particular.
 * We need to have it in core because we parse the schema here. At run time,
 * This will be overridden by server or client side services.
 *
 */
public interface HooksService {

	/**
	 * Initialize a hook.
	 */
	void initHook(Hook hook, ParserResponse parserResponse);

	public class Null implements HooksService {

      @Override
      public void initHook(Hook hook, ParserResponse parserResponse) {}
	   
	}
}

