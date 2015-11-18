package com.variant.web;

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
	
	/**
	 * Are two paths the same?
	 * Compare as strings, plus:
	 * 1. Ignore case
	 * 2. Ignore trailing slash, if present.
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static boolean pathMatches(String path1, String path2) {
		String p1 = path1.endsWith("/") ? path1.substring(0, path1.length() - 2) : path1;
		String p2 = path2.endsWith("/") ? path2.substring(0, path2.length() - 2) : path2;
		return p1.equalsIgnoreCase(p2);
		
	}
	
}
