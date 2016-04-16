package com.variant.client.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Igor
 *
 */
public class VariantWebUtils {

	/**
	 * Reconstruct request URL.
	 * @param request
	 * @return
	 */
	public static String requestUrl(HttpServletRequest request) {
		
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();
		return uri + (queryString == null ? "" : "?" + queryString);
	}
	
}
