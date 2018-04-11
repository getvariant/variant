package com.variant.client.impl;

import com.variant.client.VariantClient;


/**
 * Bootstraps and keeps track of connections. One per client.
 * At some point HTTP connection pool will prob. go here.
 * As well, we could close connections that have been idle for too long.
 * 
 * @author Igor.
 */
public class ConnectionFactory {
	
	/**
	 */
	public ConnectionImpl connectTo(VariantClient client, String schema) {
		ConnectionImpl conn = new ConnectionImpl(client, schema);
		return conn;
	}
	
}