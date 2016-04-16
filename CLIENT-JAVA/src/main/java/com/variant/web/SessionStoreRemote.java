package com.variant.web;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.session.VariantSessionImpl;
import com.variant.web.http.HttpClient;
import com.variant.web.http.HttpResponse;
import com.variant.web.http.VariantHttpClientException;

public class SessionStoreRemote implements VariantSessionStore {

	private static final Logger LOG = LoggerFactory.getLogger(SessionStoreRemote.class);
	
	private static final String LOCAL_KEY = "variant-session";
	private String apiEndpointUrl = null;
	private VariantCoreImpl coreApi;
	private long sessionTimeoutMillis;

	/**
	 * GET or create session by ID.
	 * In 0.6 we're not able to create session on the server because the server
	 * does not understand schemas. To avoid an extra trip to the server, we'll
	 * pre-create a blank session and send it in the body of the GET request,
	 * 
	 * We cash variant session in http request for idempotency, i.e. subsequent
	 * calls to this method with the same arguments will return exact same object.
	 * 
	 * @since 0.6
	 */
	@Override
	public VariantSession get(String sessionId, Object... userData) {

		if (sessionId == null || sessionId.length() == 0) {
			throw new IllegalArgumentException("No session ID");
		}
		
		HttpServletRequest httpReq = (HttpServletRequest) userData[0];
		
		// Try local cache first.
		VariantSession result = (VariantSession) httpReq.getAttribute(LOCAL_KEY);
		if (result != null) {
			if (System.currentTimeMillis() - result.creationTimestamp() < sessionTimeoutMillis) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Local session cache hit for ID [" + sessionId + "]");
				}
				return result;
			}
		}
		
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(apiEndpointUrl + "session/" + sessionId);

		if (resp.getStatus() == HttpStatus.SC_OK) {
			result = VariantSessionImpl.fromJson(coreApi, resp.getBody());
		}
		else if (resp.getStatus() == HttpStatus.SC_NO_CONTENT) {
			result = new VariantSessionImpl(coreApi, sessionId);
			save(result, userData);
		}
		else {
			throw new VariantHttpClientException(resp);
		}
		return result;
	}

	/**
	 * Save the session in the remote server.
	 */
	@Override
	public void save(VariantSession session, Object... userData) {

		if (session == null) {
			throw new IllegalArgumentException("No session");
		}

		// Write through local cache.
		HttpServletRequest httpReq = (HttpServletRequest) userData[0];
		httpReq.setAttribute(LOCAL_KEY, session);
		
		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.put(apiEndpointUrl + "session/" + session.getId(), ((VariantSessionImpl)session).toJson());
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * 
	 */
	@Override
	public void initialized(InitializationParams initParams) throws Exception {

		apiEndpointUrl = (String) initParams.getOrThrow(
				"apiEndpoint", 
				new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName(), VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()));
		
		sessionTimeoutMillis = (Integer) initParams.getOr("sessionTimeoutSecs",  "900") * 1000;
		
		coreApi = (VariantCoreImpl) initParams.getCoreApi();
	}

	@Override
	public void shutdown() {}
	
}
