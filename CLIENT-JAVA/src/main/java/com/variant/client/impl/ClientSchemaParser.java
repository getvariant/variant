package com.variant.client.impl;

import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.schema.parser.HooksService;

/**
 * Client side schema parser is the same as core, but uses a null hooker
 * because there are no user hooks on the client, and a null flusher
 * because we don't flush events on the client either.
 * 
 * @author Igor.
 *
 */
public class ClientSchemaParser extends SchemaParser {

	@Override
	public HooksService getHooksService() {
		return HooksService.NULL;
	}

	@Override
	public FlusherService getFlusherService() {
		return FlusherService.NULL;
	}

}
