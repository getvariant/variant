package com.variant.client;


import com.typesafe.config.Config;
import com.variant.client.impl.VariantClientImpl;

/**
 * Variant Bare Java Client. Makes no assumptions about the host application other than 
 * it is Java. 
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * Externally supplied configuration.
	 * See https://github.com/typesafehub/config for details on Typesafe Config.
	 * 
	 * @return An instance of the {@link Config} type.
	 * 
	 * @since 0.7
	 */
	public Config getConfig();
	
	/**
	 * Attempts to get a connection to the given server URL.
	 * 
	 * @param URL string of the form {@code <server-URI>:<schema-name>}. E.g. {@code https://localhost/variant:my-schema}
	 *        is a valid URL.
	 *        
	 * @return An instance of the {@link Connection} type.
	 * 
	 * @since 0.7
	 */
	public Connection getConnection(String url);
		
	/**
	 * Factory class: call <code>getInstance()</code> to obtain a new instance of {@link VariantClient}.
	 * 
	 * @since 0.6
	 */
	public static class Factory {
		
		/**
		 * Obtain a new instance of {@link VariantClient}.
		 * Host application should hold on to and reuse the object returned by this method whenever possible.
		 * One of these per address space is recommended.
		 * 
		 * @return Instance of the {@link VariantClient} type.
		 * @since 0.6
		 */
		public static VariantClient getInstance() {
			
			return new VariantClientImpl();
		}

	}
		
	
}
