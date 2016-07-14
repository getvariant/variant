package com.variant.client.servlet.adapter;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.client.impl.VariantInitParamsImpl;
import com.variant.client.servlet.util.TargetingTrackerString;
import com.variant.client.servlet.util.VariantCookie;
import com.variant.core.VariantProperties;

public class TargetingTrackerHttpCookie extends TargetingTrackerString {

	private TargetingCookie cookie;
	private Collection<Entry> entries = new ArrayList<Entry>();
	private VariantClient client;
	
	/**
	 * 
	 */
	private void _initialized(VariantInitParamsImpl initParams, HttpServletRequest request) {
		client = initParams.getVariantClient();
		cookie = new TargetingCookie(request);
		String input = cookie.getValue();
		// If the targeting cookie existed and returned a value, the superclass will parse it.
		if (input != null) entries = fromString(cookie.getValue(), initParams.getVariantClient().getSchema());
	}

	/**
	 * 
	 */
	private void _save(HttpServletResponse response) {		
		cookie.setValue(toString(entries));
		cookie.send(response);
	}

	//private Logger LOG = LoggerFactory.getLogger(TargetingTrackerHttpCookie.class);
	
	/**
	 * The cookie which tracks the experiences
	 */
	private static class TargetingCookie extends VariantCookie {

		private static final String COOKIE_NAME = "vrnt-target";

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

	/**
	 * User data is expected as an <code>HttpServletRequest</code> object.
	 */
	@Override
	public void initialized(VariantInitParams initParams, Object... userData) throws Exception {
		_initialized((VariantInitParamsImpl) initParams, (HttpServletRequest) userData[0]);
	}		

	/**
	 * User data is expected as an <code>HttpServletResponse</code> object.
	 */
	@Override
	public void save(Object...userData) {
		_save((HttpServletResponse) userData[1]);
	}

	@Override
	public Collection<Entry> get() {
		return entries;
	}

	@Override
	public void set(Collection<Entry> entries) {
		this.entries = entries;
	}

}
