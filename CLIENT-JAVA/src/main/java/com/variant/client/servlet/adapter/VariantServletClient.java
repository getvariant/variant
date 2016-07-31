package com.variant.client.servlet.adapter;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.VariantClient;
import com.variant.client.servlet.adapter.impl.ServletClientImpl;
import com.variant.core.VariantSession;

/**
 * <p>Servlet-aware wrapper around {@link VariantClient} that replaces all environment contingent
 * method signatures with ones operating on Servlet API types. Host applications should use this
 * interface if they are Web applicaitons running on top of the Servlet API.
 * 
 * @author Igor Urisman
 * 
 * @see {@link VariantClient}.
 * 
 * @since 0.6
 */
public interface VariantServletClient extends VariantClient {
	
	/**
	 * <p>Get user's Variant session. 
	 * 
	 * @param userData
	 * @since 0.6
	 * @return
	 */
	public VariantSession getSession(HttpServletRequest request);

	/**
	 * <p>Get user's Variant session. 
	 * 
	 * @param userData
	 * @since 0.6
	 * @return
	 */
	public VariantSession getSession(HttpServletRequest request, boolean create);

	public static class Factory {
		/**
		 * <p>Instantiate an instance of Variant client Servlet adapter. Takes 0 or more of String arguments. 
		 * If supplied, each argument is understood as a Java class path resource name. Each 
		 * resource is expected to contain a set of application properties, as specified by 
		 * Java's Properties.load() method. When VariantClient needs to look up a property
		 * value, these files are scanned left to right and the first value found is used. 
		 * If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * a default is used.
		 * 
		 * <p>Host application should hold on to and reuse the object returned by this method.
		 * Not thread safe: the host application should not use more than one of these at a time.
		 * 
		 * @since 0.6
		 */		
		public static VariantServletClient getInstance(String...resourceNames) {
			return new ServletClientImpl(resourceNames);
		}

	}
}
