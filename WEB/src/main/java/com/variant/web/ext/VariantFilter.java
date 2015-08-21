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

import com.variant.core.ParserResponse;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.error.ParserError;
import com.variant.core.error.Severity;
import com.variant.core.schema.View;
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
	
	boolean isEngineUsable = false;
	
	
	/**
	 * 
	 * @param request
	 * @return a VariantViewRequest object, if the request url is mapped in Variant schema,
	 *         or null otherwise.
	 */
	private VariantViewRequest doPreFilter(HttpServletRequest request) {
				
		// is this request's path mapped in Varianit?
		String path = VariantWebUtils.requestUrl(request);
		View view = null;
		for (View v: VariantWeb.getSchema().getViews()) {
			if (v.getPath().equalsIgnoreCase(path)) {
				view = v;
				break;
			}
		}
		
		if (view == null) return null;
		VariantSession session = VariantWeb.getSession(request);
		return VariantWeb.startViewRequest(session, view);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	private void doAfterFilter(VariantViewRequest varuiantRequest, HttpServletResponse httpResponse) {
		

	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Webapp is starting up.
	 * Initialize the Variant container and parse the test schema.
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {

		VariantWeb.bootstrap();

		String path = config.getInitParameter("schemaResourcePath");
		if (path == null) throw new ServletException("Init parameter 'schema' must be supplied");
		InputStream is = VariantIoUtils.class.getResourceAsStream(path);
		
		if (is == null) {
			throw new RuntimeException("Classpath resource by the name [" + path + "] does not exist.");
		}
				
		ParserResponse resp = VariantWeb.parseSchema(is);
		if (resp.highestErrorSeverity().greaterOrEqualThan(Severity.ERROR)) {
			LOG.error("Unable to parse Variant test schema due to following parser error(s):");
			for (ParserError error: resp.getErrors()) {
				LOG.error(error.toString());
			}
		}
		else {
			isEngineUsable = true;
			if (resp.hasErrors()) {
				LOG.warn("Variant test schema parsed with following warning(s):");
				for (ParserError error: resp.getErrors()) {
					LOG.error(error.toString());
				}
			}
		}
		LOG.debug("************************");
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
		
		if (isEngineUsable) {
			long start = System.currentTimeMillis();
			try {

				// If requested path is not instrumented, variantRequest will be null.
				variantRequest = doPreFilter(httpRequest);
				
				if (variantRequest != null ) {
					if (LOG.isDebugEnabled()) {
						String msg = 
								"Variant dispatcher for path [" + variantRequest.getView().getPath() +
								"] completed in " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS") +". ";
						if (variantRequest.isForwarding()) {
							msg += "Forward to path [" + variantRequest.resolvedViewPath() + "].";
						}
						else {
							msg += "Falling through to requested URL";
						}
						LOG.debug(msg);
					}
					
					if (variantRequest.isForwarding()) {
						request.getRequestDispatcher(variantRequest.resolvedViewPath()).forward(request, response);
						return;
					}
				}
			}
			catch (Throwable t) {
				LOG.error("Unhandled exception in Variant for path [" + VariantWebUtils.requestUrl(httpRequest) + "]", t);
			}
		}
				
		chain.doFilter(request, response);	
	
		if (isEngineUsable && variantRequest != null) {
			try {
				doAfterFilter(variantRequest, httpResponse);
			}
			catch (Throwable t) {
				LOG.error("Unhandled exception in Variant for path [" + VariantWebUtils.requestUrl(httpRequest) + "]", t);
			}
			
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
