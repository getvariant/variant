package com.variant.client.mock;

import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpServletResponse partial mock adds state and methods to get to it.
 */
public abstract class HttpServletResponseMock implements HttpServletResponse {
	private ArrayList<Cookie> addedCookies = null;
	
	@Override public void addCookie(Cookie cookie) {
		if (addedCookies == null) addedCookies = new ArrayList<Cookie>();
		addedCookies.add(cookie);
	}
	
	public Cookie[] getCookies() { 
		return addedCookies == null ? new Cookie[0] : addedCookies.toArray(new Cookie[addedCookies.size()]); 
	}
}
