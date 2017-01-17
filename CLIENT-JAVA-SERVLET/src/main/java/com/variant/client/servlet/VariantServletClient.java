package com.variant.client.servlet;

import java.util.Properties;

import com.variant.client.VariantClient;
import com.variant.client.servlet.impl.ServletClientImpl;

/**
 * <p>Variant Client Servlet adapter. 
 * Wraps the bare Java client ({@link VariantClient}) with the purpose of 1) adding environment-bound
 * methods to be used in place of bare client's environment dependent methods, e.g. 
 * {@link VariantClient#getSession(Object...)}; and 2) overriding return types of some methods with
 * servlet specific types. Host applications built of top of the Servlet API should use this client.
 * 
 * @author Igor Urisman
 * 
 * @see VariantClient
 * 
 * @since 0.6
 */
public interface VariantServletClient extends VariantClient {
	
	/**
	 * Override VariantClient's method with the servlet-aware return type..
	 * 	 *        
	 * @return An instance of the {@link VariantServletConnection} type.
	 * 
	 * @see VariantClient#getConnection(String)
	 * @since 0.7
	 */
	@Override
	public VariantServletConnection getConnection(String url);

	/**
	 * Factory class: call <code>getInstance()</code> to obtain a new instance of {@link VariantServletClient}.
	 * @author Igor Urisman
	 * @since 0.6
	 */
	public static class Factory {
		
		private Factory() {}
		
		/**
		 * Obtain a new instance of {@link VariantServletClient}. Takes zero or more String arguments
		 * which are understood to be Java classpath resource names. Each resource is expected 
		 * to contain a set of system property definitions, as specified by Java's 
		 * {@link Properties#load(java.io.Reader)} method. When Variant client needs to look 
		 * up a property value, these files are examined left to right and the first value found is
		 * used.  If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * a default is used.
		 * 
		 * Host application should hold on to and reuse the object returned by this method.
		 * 
		 * @param  resourceNames Zero or more system property files as classpath resource names.
		 * @return Instance of the {@link VariantServletClient} type.
		 * @since 0.6
		 */		
		public static VariantServletClient getInstance() {
			return new ServletClientImpl();
		}
	}
}
