package com.variant.sample;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.variant.core.ParserResponse;
import com.variant.core.error.ParserError;
import com.variant.core.error.Severity;
import com.variant.core.util.VariantIoUtils;
import com.variant.web.VariantWeb;

public class VariantFilter implements Filter {

	private static final Logger LOG = Logger.getLogger(VariantFilter.class);
	
	boolean isEngineOk = false;
	
	public void init(FilterConfig config) throws ServletException {

		VariantWeb.bootstrap();

		String schemaResourceName = config.getInitParameter("schema");
		if (schemaResourceName == null) throw new ServletException("Init parameter 'schema' must be supplied");
		InputStream is = VariantIoUtils.class.getResourceAsStream(schemaResourceName);
		
		if (is == null) {
			throw new RuntimeException("Classpath resource by the name [" + schemaResourceName + "] does not exist.");
		}
				
		ParserResponse resp = VariantWeb.parseSchema(is);
		if (resp.highestErrorSeverity().greaterOrEqualThan(Severity.ERROR)) {
			LOG.error("Unable to parse Variant test schema due to following parser error(s):");
			for (ParserError error: resp.getErrors()) {
				LOG.error(error.toString());
			}
		}
		else {
			isEngineOk = true;
			if (resp.hasErrors()) {
				LOG.warn("Variant test schema parsed with following warning(s):");
				for (ParserError error: resp.getErrors()) {
					LOG.error(error.toString());
				}
			}
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		chain.doFilter(request, response);	
	}

	public void destroy() {
		// ?
	}

}
