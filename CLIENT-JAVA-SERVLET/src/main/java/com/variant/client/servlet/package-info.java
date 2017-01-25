/**
 * <p>Entry point into the Servlet Adapter for Variant Java Client.
 * 
 * <p>This Servlet Adapter is a wrapper API around the bare Java Client, adding environment-bound
 * signatures to be used in place of their generic counterparts. For example, 
 * it provides {@link com.variant.client.servlet.ServletConnection#getSession(javax.servlet.http.HttpServletRequest)}
 * in place of the bare {@link com.variant.client.Connection#getSession(Object...)}.
 * 
 * @since 0.6
 */
package com.variant.client.servlet;
