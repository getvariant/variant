package com.variant.core.net;

import java.util.Map;

import com.variant.core.schema.Schema;
import com.variant.core.session.CoreSession;

public class SessionPayloadReader extends PayloadReader<CoreSession> {

	/**
	 * 
	 * @param core
	 * @param payload
	 */
	public SessionPayloadReader(Schema schema, String payload) {
		super(schema, payload);
	}

	/**
	 * Deserealizer.
	 */
	@Override
	protected CoreSession deserealizeBody(Schema schema, Map<String, ?> mappedJson) {
		return new CoreSession(mappedJson, schema);
	}

}
