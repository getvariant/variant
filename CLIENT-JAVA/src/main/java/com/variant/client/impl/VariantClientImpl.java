package com.variant.client.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.typesafe.config.Config;
import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.conn.ConnectionFactory;
import com.variant.client.conn.ConnectionImpl;
import com.variant.core.util.VariantConfigLoader;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantClientImpl implements VariantClient {
		
	private final Config config = new VariantConfigLoader("/variant.conf", "/com/variant/client/variant-default.conf").load();
	private final ConnectionFactory connFactory = new ConnectionFactory();
	private final ConcurrentHashMap<String, Connection> connMap = new ConcurrentHashMap<String, Connection>();
	
	/**
	 * Handshake with the server.
	 * @param payloadReader
	 *
	private void handshake(Payload payloadReader) {
		// Nothing for now.
	}
	*/
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl() {
		
	}

	/**
	 */
	@Override
	public Connection getConnection(String schema) {
		ConnectionImpl result = connFactory.connectTo(this, schema);
		connMap.put(result.getId(), result);
		return result;
	}
	
	/**
	 */
	@Override
	public Config getConfig() {
		return config;
	}

	//---------------------------------------------------------------------------------------------//
	//                                      PUBLIC EXT                                             //
	//---------------------------------------------------------------------------------------------//
	public void freeConnection(Connection conn) {
		connMap.remove(conn.getId());
	}
}
