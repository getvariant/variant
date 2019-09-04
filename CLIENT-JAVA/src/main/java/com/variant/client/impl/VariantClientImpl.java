package com.variant.client.impl;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantError;
import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.util.MethodTimingWrapper;
import com.variant.core.util.immutable.ImmutableMap;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantClientImpl implements VariantClient {

	final private static Logger LOG = LoggerFactory.getLogger(VariantClientImpl.class);

	public final ImmutableMap<String, Object> props;
	public final Server server;
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl(ImmutableMap<String, Object> props) {

		this.props = props;
		server = new Server(this);
				
     	if (LOG.isDebugEnabled()) {
     		for (Map.Entry<String, Object> e : props.entrySet()) {
     			LOG.debug(String.format("  %s => [%s]", e.getKey(), e.getValue()));
     		}
        }

	}
	
	@Override
	public Connection connectTo(String stringUri) {

		return new MethodTimingWrapper<Connection>().exec( () -> {
			// Parse the uri param
			URI uri = URI.create(stringUri);
			if (uri.getHost() == null || uri.getPath() == null || uri.getPort() < 0) {
				throw new VariantException(VariantError.MALFORMED_VARIANT_URI, stringUri);
			}
	
			String schema = uri.getPath().substring(1);  // lose the leading /
	
			return new ConnectionImpl(this, schema, server.connect(uri));
		});
	}

}
