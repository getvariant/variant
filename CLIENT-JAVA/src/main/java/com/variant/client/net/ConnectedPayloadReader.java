package com.variant.client.net;

import com.variant.client.Connection;

public abstract class ConnectedPayloadReader<T> extends AbstractPayloadReader<T> {

	protected final Connection conn;
	
	public ConnectedPayloadReader(Connection conn, String payload) {
		super(payload);
		this.conn = conn;
	}

}
