package com.variant.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;

import com.variant.core.VariantSession;
import com.variant.core.ext.TargetingPersisterString;
import com.variant.web.util.VariantCookie;

public class TargetingPersisterHttpCookie extends TargetingPersisterString {

	private TargetingCookie cookie;
	
	/**
	 * User data is expected as an <code>HttpServletRequest</code> object.
	 */
	@Override
	public void initialized(VariantSession ssn, Object userData) {
		
		HttpServletRequest request = (HttpServletRequest) userData;
		cookie = new TargetingCookie(request);
		String input = cookie.getValue();
		// If the targeting cookie existed and returned a value, the superclass will parse it.
		if (input != null) super.initialized(ssn, cookie.getValue());
	}

	/**
	 * User data is expected as an <code>HttpServletResponse</code> object.
	 */
	@Override
	public void persist(Object userData) {
		
		HttpServletResponse response = (HttpServletResponse) userData;
		cookie.setValue(toString());
		cookie.send(response);
	}

	/**
	 *
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
		protected int getMaxAge() {
			return (int) DateUtils.MILLIS_PER_DAY / 1000 * 365;
		}
		
	}

	
}
