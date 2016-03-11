package com.variant.webnative.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;
import com.variant.core.test.BaseTestCommon;
import com.variant.core.util.VariantStringUtils;
import com.variant.webnative.SessionIdTrackerHttpCookie;
import com.variant.webnative.VariantWebnative;
import com.variant.webnative.VariantWebnativeTestFacade;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BaseTest extends BaseTestCommon {
	
	private static Boolean sqlSchemaCreated = false;

	protected VariantWebnative api = null;
	
	/**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 */
	@Before
	public void _beforeTestCase() throws Exception {
		api = rebootApi();                 // in each instance 
		synchronized (sqlSchemaCreated) {  // once per JVM
			if (!sqlSchemaCreated) {
				recreateSchema();
				sqlSchemaCreated = true;
			}
		}
	}
	
	/**
	 * Subclasses will be able to override this
	 */
	protected VariantWebnative rebootApi() {
		return new VariantWebnative("/variant-test.props");
	}

	@Override
	protected JdbcService getJdbcService() {
		return new JdbcService(VariantWebnativeTestFacade.getCoreApi(api));
	}

	@Override
	protected Schema getSchema() {
		return api.getSchema();
	}

	//---------------------------------------------------------------------------------------------//
	//                                      Mockito Mocks                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Mock HttpServletRequest
	 * @return
	 */
	protected HttpServletRequest mockHttpServletRequest(String jsessionId, String vsessionId) { 
		
		//
		// Session
		//
		HttpSessionWrap ssn = mock(HttpSessionWrap.class, new DefaultAnswer());
		when(ssn.getId()).thenReturn(jsessionId);

		// Request
		HttpServletRequest result = mock(HttpServletRequest.class, new DefaultAnswer());
		when(result.getSession()).thenReturn(ssn);
		Cookie[] cookies = { new Cookie(SessionIdTrackerHttpCookie.COOKIE_NAME, vsessionId) };
		when(result.getCookies()).thenReturn(cookies);
		
		return result;
	}

	/**
	 * Mock HttpServletResponse
	 * @return
	 */
	protected HttpServletResponse mockHttpServletResponse() {
		
		//
		// Response
		//
		HttpServletResponseWrap result = mock(HttpServletResponseWrap.class, new DefaultAnswer());

		return result;
	}

	/**
	 * HttpSession wrapper adds state and methods to get to it.
	 */
	public static abstract class HttpSessionWrap implements HttpSession {
		
		private HashMap<String, Object> attributes = null;
		
		@Override public void setAttribute(String key, Object value) {
			if (attributes == null) attributes = new HashMap<String, Object>();
			attributes.put(key, value);
		}
		
		@Override public Object getAttribute(String key) {
			return attributes == null ? null : attributes.get(key);
		}
	}

	/**
	 * HttpServletResponse wrapper adds state and methods to get to it.
	 */
	public static abstract class HttpServletResponseWrap implements HttpServletResponse {
		private ArrayList<Cookie> addedCookies = null;
		
		@Override public void addCookie(Cookie cookie) {
			if (addedCookies == null) addedCookies = new ArrayList<Cookie>();
			addedCookies.add(cookie);
		}
		
		public Cookie[] getCookies() { 
			return addedCookies == null ? new Cookie[0] : addedCookies.toArray(new Cookie[addedCookies.size()]); 
		}
	}
	
	/**
	 * In implementation of Answer that overrides Mockito's defaults.
	 * 1. Unstubbed void methods will throw an exception, instead of doing nothing. 
	 * 2. ?
	 * Note that doing the same to methods returning a value will make it impossible to use the when() style
	 * stubbing, so we're not doing that.
	 */
	public static class DefaultAnswer implements Answer<Object> {
		
		@Override 
		public Object answer(InvocationOnMock invoc) throws Throwable {
			
			// If there's a concrete method - call it.
			if (!isMethodAbstract(invoc)) return Answers.CALLS_REAL_METHODS.answer(invoc);

			// Otherwise, if it's a void method, we're processing an unstubbed void which by default does nothing
			// We override that with an exception.
			if (invoc.getMethod().getReturnType() == Void.TYPE) {
				throw new UnsupportedOperationException(
						String.format("Unstubbed void method [%s] on [%s]", invoc.getMethod().getName(), invoc.getMock()));
			}
			
			// Otherwise, we're unstubbed non-void method and are probably returning null.
			return Answers.RETURNS_DEFAULTS.answer(invoc);		
		}
		
		private static boolean isMethodAbstract(InvocationOnMock invoc) {
			return Modifier.isAbstract(invoc.getMethod().getModifiers());
		}
	}
}

