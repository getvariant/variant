package com.variant.client.conn;

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
	protected HooksService getHooksService() {
		return HooksService.NULL;
	}

	@Override
	protected FlusherService getFlusherService() {
		return FlusherService.NULL;
	}

	
}
