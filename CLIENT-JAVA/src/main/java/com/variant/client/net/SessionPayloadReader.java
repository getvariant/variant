package com.variant.client.net;

import java.util.Map;

import com.variant.client.Connection;
import com.variant.core.session.CoreSession;

public class SessionPayloadReader extends ConnectedPayloadReader<CoreSession> {

	/**
	 * 
	 * @param core
	 * @param payload
	 */
	public SessionPayloadReader(Connection conn, String payload) {
		super(conn, payload);
	}

	/**
	 * Parser.
	 */
	@Override
	protected AbstractPayloadReader.AbstractParser<CoreSession> parser() {

		return new AbstractPayloadReader.AbstractParser<CoreSession>() {
			@Override
			public CoreSession parse(Map<String, ?> mappedJson) {
				return CoreSession.fromJson(mappedJson, conn.getSchema());
			}
		};
	}
}
