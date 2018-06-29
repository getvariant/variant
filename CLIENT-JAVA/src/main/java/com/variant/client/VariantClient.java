package com.variant.client;


import java.util.concurrent.CompletableFuture;

import com.typesafe.config.Config;
import com.variant.client.impl.VariantClientImpl;

/**
 * Variant Java Client object.
 * <p>
 * Provides connectivity to Variant Experience Server from any JVM process. 
 * Makes no assumptions about the host application other than 
 * it is running on a JVM. 
 * 
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * Externally supplied configuration.
	 * See Variant Java Client User Guile for details on configuring Variant Java client.
	 * 
	 * @return An object of type <a href="https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html" target="_blank">com.typesafe.config.Config</a>.
	 * 
	 * @since 0.7
	 */
	public Config getConfig();
	
	/**
	 * Connect to the given variation schema on the server.
	 * 
	 * @param schema The name of the schema, which should be deployed on the server.
	 *        
	 * @return An instance of the {@link Connection} type, or <code>null</null> if the
	 * requested schema was not found on the server.
	 * 
	 * @since 0.7
	 *
	public Connection getConnection(String schema);
	*/
	
	public CompletableFuture<Connection> connectTo(String schema);
	
	/**
	 * Static factory class: call {@link #getInstance()} to obtain a new instance of {@link VariantClient}.
	 * 
	 * @since 0.6
	 */
	public static class Factory {
		
		/**
		 * Obtain a new instance of {@link VariantClient}.
		 * Host application should hold on to and reuse the object returned by this method whenever possible.
		 * One of these per process is recommended.
		 * 
		 * @return Instance of the {@link VariantClient} type.
		 * @since 0.6
		 */
		public static VariantClient getInstance() {
			
			return new VariantClientImpl();
		}
	}
}
