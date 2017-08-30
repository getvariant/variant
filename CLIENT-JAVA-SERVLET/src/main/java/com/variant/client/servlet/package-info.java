/**
 * Variant Java Client Servlet Adapter top-level objects 
 * necessary to obtain and manipulate an instance of Variant Java Client Servlet Adapter.
 * Variant Servlet Adapter is a wrapper around the bare Java Client, which adds environment
 * bound signatures to be used in place of their environment dependent counterparts. The original
 * environment dependent signatures may still be used in conjunction with custom session ID
 * tracker or targeting tracker whose init() and save() methods should take something other than 
 * HttpServletRequest and HttpServletResponse respectively. 
 * 
 * @see com.variant.client.VariantSessionIdTracker
 * @see com.variant.client.VariantTargetingTracker
 * 
 * @since 0.6
 */
package com.variant.client.servlet;
