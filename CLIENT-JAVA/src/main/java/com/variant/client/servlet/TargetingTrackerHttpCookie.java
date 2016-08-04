package com.variant.client.servlet;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.client.servlet.util.VariantCookie;
import com.variant.client.session.TargetingTrackerString;
import com.variant.core.VariantProperties;

public class TargetingTrackerHttpCookie extends TargetingTrackerString {

	private TargetingCookie cookie;
	private VariantClient client;
	
	//private Logger LOG = LoggerFactory.getLogger(TargetingTrackerHttpCookie.class);
	
	/**
	 * The cookie which tracks the experiences
	 */
	private static class TargetingCookie extends VariantCookie {

		private TargetingCookie() {
			super(COOKIE_NAME);
		}
		
		private TargetingCookie(HttpServletRequest request) {
			super(COOKIE_NAME, request);
		}

		/**
		 * We want this cookie to never expire, so set a year in the future.
		 */
		@Override
		public int getMaxAge() {
			return (int) DateUtils.MILLIS_PER_DAY / 1000 * 365;
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public static final String COOKIE_NAME = "variant-target";

	/**
	 * Superclass needs properties but doesn't have them.
	 */
	@Override
	protected VariantProperties getProperties() {
		return client.getProperties();
	}

	/**
	 * User data is expected as an <code>HttpServletRequest</code> object.
	 */
	@Override
	public void init(VariantInitParams initParams, Object...userData){
		client = initParams.getVariantClient();
		HttpServletRequest request =  (HttpServletRequest) userData[0];
		cookie = new TargetingCookie(request);
	}		

	/**
	 * User data is expected as an <code>HttpServletResponse</code> object.
	 */
	@Override
	public void save(Object...userData) {
		HttpServletResponse response = (HttpServletResponse) userData[0];
		cookie.send(response);
	}

	/**
	 * Expecting userData[0] to be the HttpServletRequest.
	 */
	@Override
	public Collection<Entry> get() {
		String input = cookie.getValue();
		// If the targeting cookie existed and returned a value, the superclass will parse it.
		return input == null ? null : fromString(cookie.getValue(), client.getSchema());
	}

	@Override
	public void set(Collection<Entry> entries) {
		cookie.setValue(toString(entries));
	}

}
