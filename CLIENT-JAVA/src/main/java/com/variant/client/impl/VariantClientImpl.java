package com.variant.client.impl;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.server.boot.ConfigLoader;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantClientImpl implements VariantClient {
		
	final private static Logger LOG = LoggerFactory.getLogger(VariantClientImpl.class);
	private final Config config = ConfigLoader.load("/variant.conf", "/com/variant/client/variant-default.conf");
	
	public final Server server;
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl(Properties props) {
		server = new Server(this);
		
        // Echo all config keys if debug
     	if (LOG.isDebugEnabled()) {
     		for (Map.Entry<String, ConfigValue> e : config.entrySet()) {
     			LOG.debug(String.format("  %s => [%s]", e.getKey(), e.getValue()));
     		}
        }

	}
	
	/**
	 */
	@Override
	public Config getConfig() {
		return config;
	}

	@Override
	public Connection connectTo(String schema) {
		
		return new ConnectionImpl(this, schema, server.connect(schema));
	}

}
