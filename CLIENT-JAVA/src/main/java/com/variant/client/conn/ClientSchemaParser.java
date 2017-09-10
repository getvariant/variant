package com.variant.client.conn;

import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.schema.parser.HooksService;

/**
 * Client side schema parser is the same as core, but uses a null hooker
 * because there are no user hooks on the client.
 * 
 * @author Igor.
 *
 */
public class ClientSchemaParser extends SchemaParser {

	@Override
	protected HooksService getHooker() {
		return HooksService.NULL;
	}

	
}