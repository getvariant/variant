package com.variant.client.servlet;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.VariantClient;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.servlet.impl.ServletClientImpl;
import com.variant.core.VariantSession;
import com.variant.core.exception.VariantSchemaModifiedException;

/**
 * <p>Variant client servlet adapter. 
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
	 * <p>Get or, if does not exist, create user's Variant session. 
	 * Environment bound variant of {@link #getOrCreateSession(Object...)}. Use in conjunction with
	 * the HTTP Cookie provided implementations of the session ID and targeting trackers, which both
	 * expect the current {@link HttpServletRequest} as a sole argument.
     *
	 * <p>If the session ID exists in the underlying implementation 
	 * of {@link VariantSessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned. Otherwise, a new session is created. If the session has not expired but the 
	 * schema has changed since it was created, this call will throw an unchecked 
	 * {@link VariantSchemaModifiedException}.
	 * 
	 * @param request Current HttpServletRequest.
	 * @return An and instance of {@link VariantSession}, enriched for the servlet environment.
	 * 
	 * @since 0.6
	 * @see SessionIdTrackerHttpCookie
	 * @see TargetingTrackerHttpCookie
	 * 
	 */
	public VariantServletSession getOrCreateSession(HttpServletRequest request);

	/**
	 * <p>Get user's Variant session. 
	 * 
	 * <p>If the session ID exists in the underlying implementation 
	 * of {@link VariantSessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned.  If the session has not expired but the schema has changed since it was created, 
	 * this call will throw an unchecked {@link VariantSchemaModifiedException}.
	 * 
	 * @param request Current HttpServletRequest.
	 * @return An and instance of {@link VariantSession}, enriched for the servlet environment.
	 * 
	 * @since 0.6
	 * @see SessionIdTrackerHttpCookie
	 * @see TargetingTrackerHttpCookie
	 */
	public VariantServletSession getSession(HttpServletRequest request);

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
		 * to contain a set of application property definitions, as specified by Java's 
		 * {@link Properties#load(java.io.Reader)} method. When Variant client needs to look 
		 * up a property value, these files are examined left to right and the first value found is
		 * used.  If a value wasn't found in any of the supplied files, or if no files were supplied, 
		 * a default is used.
		 * 
		 * Host application should hold on to and reuse the object returned by this method.
		 * 
		 * @param  resourceNames Zero or more application property files as classpath resource names.
		 * @return Instance of the {@link VariantServletClient} type.
		 * @since 0.6
		 */		
		public static VariantServletClient getInstance(String...resourceNames) {
			return new ServletClientImpl(resourceNames);
		}
	}
}
