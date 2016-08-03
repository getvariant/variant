package com.variant.client.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.impl.StateSelectorByRequestPath;
import com.variant.client.servlet.util.VariantWebUtils;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;

/**
 * Copyright 2015 Variant. All rights reserved.
 * 
 * Single entry point to the Variant SGM Engine.
 * Can be deployed in front of any Servlet API application.
 * 
 * Can be configured via the following filter config parameters:
 * schemaResourcePath specifies the resource path 
 * 
 * @author Igor Urisman
 *
 */
public class VariantFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(VariantFilter.class);
	
	private VariantServletClient client;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Webapp is starting up.
	 * Initialize the Variant container and parse the test schema.
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {

		String name = config.getInitParameter("propsResourceName");
		client = name == null ? VariantServletClient.Factory.getInstance() : VariantServletClient.Factory.getInstance(name);
			
		name = config.getInitParameter("schemaResourceName");
		
		if (name == null) throw new ServletException("Init parameter 'schemaResourceName' must be supplied");
		
		InputStream is = getClass().getResourceAsStream(name);
		if (is == null) {
			throw new RuntimeException("Classpath resource by the name [" + name + "] does not exist.");
		}
						
		ParserResponse resp = client.parseSchema(is);
		Severity highSeverity = resp.highestMessageSeverity();
		if (highSeverity != null && highSeverity.greaterOrEqualThan(Severity.ERROR)) {
			LOG.error("Unable to parse Variant test schema due to following parser error(s):");
			for (ParserMessage msg: resp.getMessages()) {
				LOG.error(msg.toString());
			}
		}
		else {
			if (resp.hasMessages()) {
				LOG.warn("Variant test schema parsed with following message(s):");
				for (ParserMessage msg: resp.getMessages()) {
					LOG.error(msg.toString());
				}
			}
		}
	}

	/**
	 * Request.
	 * It is critical that we catch all Variant related exceptions and
	 * simply fall back to the control experience, i.e. the one that was
	 * requested.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		VariantServletSession variantSsn = null; 
		VariantServletStateRequest variantRequest = null;
		
		long start = System.currentTimeMillis();

		String resolvedPath = null;
		boolean isForwarding = false;
		
		try {

			// Is this request's URI mapped in Variant?
			String url = VariantWebUtils.requestUrl(httpRequest);
			State state = StateSelectorByRequestPath.select(client.getSchema(), url);
			
			if (state == null) {

				// We don't know about this path.
				if (LOG.isDebugEnabled()) {
					LOG.debug("Path [" + url + "] is not instrumented");
				}
			
			}
			else {
			
				// Yes, this path is mapped in Variant.
				variantSsn = client.getOrCreateSession(httpRequest);
				variantRequest = variantSsn.targetForState(state);
				resolvedPath = variantRequest.getResolvedParameterMap().get("path");
				isForwarding = !resolvedPath.equals(state.getParameterMap().get("path"));
				
				String msg = 
						"Variant dispatcher for URL [" + url +
						"] completed in " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS") +". ";
				if (isForwarding) {
					msg += "Forwarding to path [" + resolvedPath + "].";							
				}
				else {
					msg += "Falling through to requested URL";
				}
				LOG.info(msg);
				
			}
		}
		catch (Throwable t) {
			LOG.error("Unhandled exception in Variant for path [" + VariantWebUtils.requestUrl(httpRequest) + "]", t);
			isForwarding = false;
			if (variantRequest != null) {
				variantRequest.setStatus(VariantStateRequest.Status.FAIL);
			}
		}

		if (isForwarding) {
			request.getRequestDispatcher(resolvedPath).forward(request, response);
		}				
		else {
			chain.doFilter(request, response);							
		}
							
		if (variantRequest != null) {
			try {
				// Add some extra info to the state visited event(s)
				VariantEvent sve = variantRequest.getStateVisitedEvent();
				if (sve != null) sve.getParameterMap().put("HTTP_STATUS", httpResponse.getStatus());
				variantRequest.commit(httpResponse);
			}
			catch (Throwable t) {
				LOG.error("Unhandled exception in Variant for path [" + 
						VariantWebUtils.requestUrl(httpRequest) + 
						"] and session [" + variantRequest.getSession().getId() + "]", t);
				
				variantRequest.setStatus(VariantStateRequest.Status.FAIL);
			}
		}
	}

	/**
	 * Webapp is shutting down.
	 * Shutdown the Variant engine to ensure that all in-flight events are flushed.
	 */
	@Override
	public void destroy() {
		// nothing.
	}

}
