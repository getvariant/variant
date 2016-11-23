package com.variant.core.net;

import java.util.Map;

import com.variant.core.schema.Schema;
import com.variant.core.session.CoreSessionImpl;

public class SessionPayloadReader extends PayloadReader<CoreSessionImpl> {

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
	protected CoreSessionImpl deserealizeBody(Schema schema, Map<String, ?> mappedJson) {
		return CoreSessionImpl.fromJson(mappedJson, schema);
	}

}
