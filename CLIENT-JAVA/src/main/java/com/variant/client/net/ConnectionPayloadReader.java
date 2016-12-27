package com.variant.client.net;

import java.util.Map;

import com.variant.client.Connection;
import com.variant.client.conn.ConnectionImpl;

public class ConnectionPayloadReader extends AbstractPayloadReader<Connection> {

	/**
	 * 
	 * @param core
	 * @param payload
	 */
	public ConnectionPayloadReader(String payload) {
		super(payload);
	}

	@Override
	protected AbstractPayloadReader.AbstractParser<Connection> parser() {

		return new AbstractPayloadReader.AbstractParser<Connection>() {
			@Override
			public Connection parse(Map<String, ?> mappedJson) {
				return ConnectionImpl.fromJson(mappedJson);
			}
		};
	}

}
