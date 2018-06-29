package com.variant.client.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.typesafe.config.Config;
import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.core.conf.ConfigLoader;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantClientImpl implements VariantClient {
		
	private final Config config = ConfigLoader.load("/variant.conf", "/com/variant/client/variant-default.conf");
	private final ConnectionFactory connFactory = new ConnectionFactory(this);
	private final ConcurrentHashMap<String, Connection> connMap = new ConcurrentHashMap<String, Connection>();
	
	public final LifecycleService lceService = new LifecycleService(this);
	public final Server server;
		
	/**
	 */	
	private Connection getConnection(String schema) {
		ConnectionImpl result = connFactory.connectTo(schema);
		if (result != null) connMap.put(result.getId(), result);
		return result;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl() {
		server = new Server(this);
	}
	
	/**
	 */
	@Override
	public Config getConfig() {
		return config;
	}

	@Override
	public CompletableFuture<Connection> connectTo(String schema) {
		return CompletableFuture.completedFuture(getConnection(schema));
		
	}

	//---------------------------------------------------------------------------------------------//
	//                                      PUBLIC EXT                                             //
	//---------------------------------------------------------------------------------------------//
	public void freeConnection(String cid) {
		connMap.remove(cid);
	}
	
	/**
	 * Tests use this to confirm connetion's gone.
	 * @param id
	 * @return
	 */
	public Connection byId(String id) {
		return connMap.get(id);
	}

}
