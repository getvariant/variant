package com.variant.web.ext;

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

import com.variant.core.VariantViewRequest;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.core.util.VariantIoUtils;
import com.variant.web.VariantWeb;
import com.variant.web.util.VariantWebUtils;

/**
 * Copyright 2015 Variant. All rights reserved.
 * 
 * Single entry point to the Variant SGM Engine.
 * Can be deployed in front of any Servlet API application.
 * 
 * Can be configured via the following filter config parameters:
 * schemaResourcePath specifies the resource path 
 * 
 * @author Igor
 *
 */
public class VariantFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(VariantFilter.class);
	
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
		if (name != null) VariantWeb.bootstrap(name);
		else VariantWeb.bootstrap();
			
		name = config.getInitParameter("schemaResourceName");
		
		if (name == null) throw new ServletException("Init parameter 'schemaResourceName' must be supplied");
		
		InputStream is = VariantIoUtils.class.getResourceAsStream(name);
		if (is == null) {
			throw new RuntimeException("Classpath resource by the name [" + name + "] does not exist.");
		}
						
		ParserResponse resp = VariantWeb.parseSchema(is);
		if (resp.highestMessageSeverity().greaterOrEqualThan(Severity.ERROR)) {
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
		VariantViewRequest variantRequest = null;
		String forwardUrl = null;
		
		if (VariantWeb.isBootstrapped()) {
			
			long start = System.currentTimeMillis();
			
			try {

				// Is this request's URI mapped in Variant?
				String url = VariantWebUtils.requestUrl(httpRequest);
				State view = VariantWeb.getSchema().matchViewByPath(url);
				
				if (view == null) {

					// We don't know about this path.
					if (LOG.isDebugEnabled()) {
						LOG.debug("Path [" + url + "] is not instrumented");
					}
				
				}
				else {
				
					// Yes, this path is mapped in Variant.
					variantRequest = VariantWeb.startViewRequest(view, httpRequest);

					if (LOG.isDebugEnabled()) {

						String msg = 
								"Variant dispatcher for path [" + variantRequest.getState().getPath() +
								"] completed in " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS") +". ";
						if (variantRequest.isForwarding()) {
							msg += "Forwarding to path [" + variantRequest.resolvedViewPath() + "].";
						}
						else {
							msg += "Falling through to requested URL";
						}
						LOG.debug(msg);
					}
					
					if (variantRequest.isForwarding()) {
						forwardUrl = variantRequest.resolvedViewPath();
						if (forwardUrl == null) {
							throw new VariantInternalException("Resovled path cannot be null for session [" + variantRequest.getSession().getId() + "]");
						}
					}
				}
			}
			catch (Throwable t) {
				LOG.error("Unhandled exception in Variant for path [" + 
						VariantWebUtils.requestUrl(httpRequest) + 
						"] and session [" + variantRequest.getSession().getId() + "]", t);
				// null out variant request object so we don't attempt to do anything with it.
				variantRequest = null;
			}

			if (forwardUrl == null) {
				chain.doFilter(request, response);							
			}				
			else {
				request.getRequestDispatcher(forwardUrl).forward(request, response);
			}
								
			if (VariantWeb.isBootstrapped() && variantRequest != null) {
				try {
					variantRequest.getViewServeEvent().setParameter("HTTP_STATUS", httpResponse.getStatus());
					VariantWeb.commitViewRequest(variantRequest, httpResponse);   
				}
				catch (Throwable t) {
					LOG.error("Unhandled exception in Variant for path [" + 
							VariantWebUtils.requestUrl(httpRequest) + 
							"] and session [" + variantRequest.getSession().getId() + "]", t);
				}
			}
		}
		else {
			LOG.info("Variant is not bootstrapped.");
			chain.doFilter(request, response);
		}
	}

	/**
	 * Webapp is shutting down.
	 * Shutdown the Variant engine to ensure that all in-flight events are flushed.
	 */
	@Override
	public void destroy() {
		VariantWeb.shutdown();
	}

}