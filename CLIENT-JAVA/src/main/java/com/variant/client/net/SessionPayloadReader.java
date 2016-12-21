package com.variant.client.net;

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
	 * Parser.
	 */
	@Override
	protected CoreSession parse(Schema schema, Map<String, ?> mappedJson) {
		return CoreSession.fromJson(mappedJson, schema);
	}

}
