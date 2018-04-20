package com.variant.client;


import com.typesafe.config.Config;
import com.variant.client.impl.VariantClientImpl;

/**
 * "Bare" Variant Java Client. Makes no assumptions about the host application other than 
 * it is Java (or any other JVM language, e.g. Scala). Allows host application interact with
 * one or more Variant servers. In many cases host applications use higher level adapter APIs,
 * which provide easier to use, higher level bindings, e.g. the Servlet adapter.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * Externally supplied configuration. 
	 * See <a href="https://github.com/typesafehub/config" target="_blank">https://github.com/typesafehub/config</a>
	 * for details on Lightbend Config.
	 * 
	 * @return An instance of the com.typesafe.config.Config type.
	 * 
	 * @since 0.7
	 */
	public Config getConfig();
	
	/**
	 * Attempts to get a connection to the given experiment schema.
	 * 
	 * @param schema The name of the schema, which should be deployed on the server.
	 *        
	 * @return An instance of the {@link Connection} type, or <code>null</null> if the
	 * requested schema was not found on the server.
	 * 
	 * @since 0.7
	 */
	public Connection getConnection(String schema);
		
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
