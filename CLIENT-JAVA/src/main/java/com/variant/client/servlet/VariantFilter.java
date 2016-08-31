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
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.ParserMessage.Severity;
import com.variant.core.xdm.State;

/**
 * <p>The preferred way of instrumenting Variant experiments for host applications written on top of the Java Servlet API.
 * 
 * <h4>Overview</h4> 
 *
 * <p>By using this filter, the application programmer can, in many cases, instrument experiments
 * with no to very little coding. The filter intercepts HTTP requests for the instrumented
 * pages, obtains the resolved variant and, if non-control, forwards the request to the path
 * contained in the resolved state parameter {@code path}.
 * 
 * <p>The application programmer should use this filter if the following assumptions hold:
 * <ul>
 * <li>The host applicaiton is written on top of the Servlet API.
 * <li>All state variants have a single point of entry, which can be mapped to a request path.
 * <li>The test schema defines the {@code path} state parameter, so that 1) its base value 
 * denotes the resource path to the this state's control; and 2) its variant value denotes the 
 * resource path to the single point of entry of that state variant.
 * </ul>
 * 
 * <h4>Configuration</h4>
 * <p>The filter is configured as following:
 * <pre>
 * {@code
 *  <filter>
 *      <filter-name>variantFilter</filter-name>
 *      <filter-class>com.variant.client.servlet.VariantFilter</filter-class> 
 *      <init-param>
 *          <param-name>schemaResourceName</param-name>
 *          <param-value>/path/to/schema.json</param-value>
 *      </init-param>
 *      <init-param>
 *          <param-name>propsResourceName</param-name>
 *          <param-value>/path/to/variant.props</param-value>
 *      </init-param>
 *  </filter>
 *   
 *  <filter-mapping>
 *      <filter-name>variantFilter</filter-name>
 *      <url-pattern>/*</url-pattern>
 *  </filter-mapping>
 * }
 * </pre>
 *
 * The {@link #init(FilterConfig)} method looks for two initialization parameters:
 * <ul>
 * <li>{@code schemaResourceName}: Name of the schema file as a classpath resource. 
 * Parsed once during filter initialization. To change a schema, application restart will be required. 
 * (Temporary limitaion to be removed in an upcoming version.)
 * 
 * <li>{@code propsResourceName}: Name of the application properties file as a
 * classpath resource. Its content will override default properties, but will be overridden
 * by {@code /varaint.props}, if found on the classpath.
 * </ul>
 * 
 * <p>The URL pattern in filter mapping can be something narrower than {@code /*}, of course, so long
 * as it matches all state {@code path}s listed in the schema.
 * 
 * <p>{@link StateParsedHookListenerImpl} hook listener is registered prior to parsing of the schema,
 * which checks that each state defines the {@code path} parameter and that its value starts with a
 * forward slash. If that is not the case, a parser error will be emitted by the listener.
 * 
 * <h4>Execution Semantics</h4>
 * 
 * <p>It is assumed that the base {@code path} state parameter, i.e. the one specified at the {@code State}
 * level, denotes the resource path to the control variation of this state. This allows this filter to
 * identify whether an incoming HTTP request is for an instrumented state or not.  If not, the request
 * is forwarded down the filter chain by calling {@code chain.doFilter(ServletRequest, ServletResponse)}.
 * 
 * <p>If the requested path corresponds to an instrumented state, the session (obtained from 
 * Variant client servlet adapter by calling {@link VariantServletClient#getOrCreateSession(HttpServletRequest)}) 
 * is targeted for this state with {@link VariantServletSession#targetForState(State)}. The resulting
 * {@link VariantServletStateRequest} object contains information about the outcome of the targeting
 * operation, including the resulting variant and the resolved state parameters. This 
 * {@link VariantServletStateRequest} object is added to the current {@link HttpServletRequest} as
 * an attribute named {@link #VARIANT_REQUEST_ATTRIBUTE_NAME}, should the downstream application code 
 * wish to extend the semantics, e.g. trigger a custom {@link VariantEvent}.  
 * 
 * <p>The resolved variant's {@code path} 
 * state parameter is interpreted as the request path to the resource which implements the targeted
 * variant. The request is forwarded to that path with {@code ServletRequestDispatcher.forward()}.
 * 
 * <p>Upon return from either forward or a fall-through down the filter chain, the 
 * {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} method below adds the status of the
 * HTTP response in progress to the pending state visited event and commits the Variant state request
 * in progress.
 * 
 * <p>Any exceptions due to Variant are caught and logged. Should such an exception occur, an attempt
 * is made to mark the state of the pending state visited event as failed and to allow the request
 * proceed down the filter chain. In other words, should the instrumentation fail due to an internal
 * Varaint exception, the user session will see the control experience. 
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class VariantFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(VariantFilter.class);
	
	private VariantServletClient client;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public static final String VARIANT_REQUEST_ATTRIBUTE_NAME = "variant-state-request";
	/**
	 * Initialize the Variant servlet adapter and parse the experiment schema.
	 * @see Filter#init(FilterConfig)
	 * @since 0.5
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
	 * Identify the state, target and commit the state request.
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 * @since 0.5
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

				// Variant doesn't know about this path.
				if (LOG.isTraceEnabled()) {
					LOG.trace("Path [" + url + "] is not instrumented");
				}
			
			}
			else {
			
				// This path is instrumented by Variant.
				variantSsn = client.getOrCreateSession(httpRequest);
				variantRequest = variantSsn.targetForState(state);
				httpRequest.setAttribute(VARIANT_REQUEST_ATTRIBUTE_NAME, variantRequest);
				resolvedPath = variantRequest.getResolvedParameterMap().get("path");
				isForwarding = !resolvedPath.equals(state.getParameterMap().get("path"));
				
				if (LOG.isDebugEnabled()) {
					String msg = 
							"Variant dispatcher for URL [" + url +
							"] completed in " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS") +". ";
					if (isForwarding) {
						msg += "Forwarding to path [" + resolvedPath + "].";							
					}
					else {
						msg += "Falling through to requested URL";
					}
					LOG.debug(msg);
				}
				
			}
		}
		catch (Throwable t) {
			LOG.error("Unhandled exception in Variant for path [" + VariantWebUtils.requestUrl(httpRequest) + "]", t);
			isForwarding = false;
			if (variantRequest != null) {
				variantRequest.setStatus(VariantCoreStateRequest.Status.FAIL);
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
				
				variantRequest.setStatus(VariantCoreStateRequest.Status.FAIL);
			}
		}
	}

	/**
	 * @see Filter#destroy()
	 * @since 0.5
	 */
	@Override
	public void destroy() {
		// nothing.
	}

}