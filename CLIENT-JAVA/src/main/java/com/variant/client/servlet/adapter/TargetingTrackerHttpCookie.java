package com.variant.client.servlet.adapter;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.client.servlet.util.TargetingTrackerString;
import com.variant.client.servlet.util.VariantCookie;
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

	/**
	 * Superclass needs properties but doesn't have them.
	 */
	@Override
	protected VariantProperties getProperties() {
		return client.getProperties();
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public static final String COOKIE_NAME = "vrnt-target";

	/**
	 * User data is expected as an <code>HttpServletRequest</code> object.
	 */
	@Override
	public void initialized(VariantInitParams initParams) throws Exception {
		client = initParams.getVariantClient();
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
	public Collection<Entry> get(Object... userData) {
		HttpServletRequest request =  (HttpServletRequest) userData[0];
		cookie = new TargetingCookie(request);
		String input = cookie.getValue();
		// If the targeting cookie existed and returned a value, the superclass will parse it.
		return input == null ? null : fromString(cookie.getValue(), client.getSchema());
	}

	@Override
	public void set(Collection<Entry> entries) {
		cookie.setValue(toString(entries));
	}

}
