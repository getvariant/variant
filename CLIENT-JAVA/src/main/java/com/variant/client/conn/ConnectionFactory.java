package com.variant.client.conn;

import com.variant.client.VariantClient;


/**
 * Bootstraps and keeps track of connections. One per client.
 * @author Igor.
 */
public class ConnectionFactory {
	
	/**
	 */
	public ConnectionImpl connectTo(VariantClient client, String url) {
		
		ConnectionImpl conn = new ConnectionImpl(client, url);
		
		return conn;
		
	}
	
}
