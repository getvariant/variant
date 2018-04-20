package com.variant.client.impl;

import com.variant.client.ClientException;
import com.variant.client.net.Payload;
import com.variant.core.ServerError;



/**
 * Bootstraps and keeps track of connections. One per client.
 * At some point HTTP connection pool will prob. go here.
 * As well, we could close connections that have been idle for too long.
 * 
 * @author Igor.
 */
class ConnectionFactory {
	
	private final VariantClientImpl client;

	ConnectionFactory(VariantClientImpl client) {
		this.client = client;
	}

	/**
	 */
	public ConnectionImpl connectTo(String schema) {
		
		try {
			Payload.Connection payload = client.server.connect(schema);
			return new ConnectionImpl(client, payload);
		}
		catch (ClientException.User ue) {
			if (ue.getError() == ServerError.UnknownSchema) 
				return null;
			throw ue;
		}		
	}
	
}
