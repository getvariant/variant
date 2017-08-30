package com.variant.client.servlet.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.Cookie;

import org.mockito.Answers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.variant.client.VariantClient;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.mock.HttpServletRequestMock;
import com.variant.client.mock.HttpServletResponseMock;
import com.variant.client.mock.HttpSessionMock;
import com.variant.client.servlet.SessionIdTrackerHttpCookie;
import com.variant.client.servlet.TargetingTrackerHttpCookie;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.impl.ServletClientImpl;
import com.variant.client.test.BareClientBaseTest;
import com.variant.core.impl.VariantCore;
import com.variant.core.util.inject.Injector;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class ServletClientBaseTest extends BareClientBaseTest {
	
	private VariantCore core;
	
	@Override
	protected VariantClient newBareClient() {
		throw new UnsupportedOperationException("Servlet Adapter tests should call newAdapterClient() instead");
	}

	protected VariantServletClient newServletAdapterClient() {
		Injector.setConfigNameAsResource("/variant/injector-servlet-adapter-local-test.json");
		ServletClientImpl result = (ServletClientImpl) VariantServletClient.Factory.getInstance("/variant/servlet-adapter-test.props");
		core = ((VariantClientImpl)result.getBareClient()).getCoreApi();
		return result;
	}

	@Override
	protected VariantCore getCoreApi() {
		return core;
	}

	//---------------------------------------------------------------------------------------------//
	//                                      Mockito Mocks                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Mock HttpServletRequest. Will contain no Variant related cookies.
	 * @return
	 */
	protected HttpServletRequestMock mockHttpServletRequest() { 
		return mockHttpServletRequest(null, (String)null);
	}

	/**
	 * Mock HttpServletRequest. Will only contain the SID cookie.
	 * @return
	 */
	protected HttpServletRequestMock mockHttpServletRequest(String sessionId) { 
		return mockHttpServletRequest(sessionId, (String)null);
	}

	/**
	 * Mock HttpServletRequest. Will contain variant related cookies as passed in arguments
	 * @return
	 */
	protected HttpServletRequestMock mockHttpServletRequest(
			String sessionId, Collection<VariantTargetingTracker.Entry> entries) { 
		
		String targetingTrackerVal = null;
		if (entries != null && entries.size() > 0) {
			targetingTrackerVal = TargetingTrackerHttpCookie.toString(entries);
		}

		return mockHttpServletRequest(sessionId, targetingTrackerVal);
	}

	/**
	 * Mock HttpServletRequest. Will contain variant related cookies from the passed response.
	 * This immulates the preservation of cookies by the browser.
	 * @return
	 */
	protected HttpServletRequestMock mockHttpServletRequest(HttpServletResponseMock resp) { 
		
		Cookie c = resp.getCookie(SessionIdTrackerHttpCookie.COOKIE_NAME);
		String sidTrackerVal = c == null ? null : c.getValue();
		
		c = resp.getCookie(TargetingTrackerHttpCookie.COOKIE_NAME);
		String targetingTrackerVal = c == null ? null : c.getValue();
		
		return mockHttpServletRequest(sidTrackerVal, targetingTrackerVal);
	}

	/**
	 * Mock HttpServletRequest. Will contain variant related cookies as passed in arguments
	 * @return
	 */
	protected HttpServletRequestMock mockHttpServletRequest(String sidTrackerVal, String targetingTrackerVal) { 

		// Session
		HttpSessionMock ssn = mock(HttpSessionMock.class, new DefaultAnswer());

		// Request
		HttpServletRequestMock result = mock(HttpServletRequestMock.class, new DefaultAnswer());
		when(result.getSession()).thenReturn(ssn);
		
		ArrayList<Cookie> cookies = new ArrayList<Cookie>();
		if (sidTrackerVal != null) {
			cookies.add(new Cookie(SessionIdTrackerHttpCookie.COOKIE_NAME, sidTrackerVal)); 	
		}

		if (targetingTrackerVal != null) {
			cookies.add(new Cookie(TargetingTrackerHttpCookie.COOKIE_NAME, targetingTrackerVal));
		}
		
		when(result.getCookies()).thenReturn(cookies.toArray(new Cookie[] {}));
		
		return result;
	}

	/**
	 * Mock HttpServletResponse
	 * @return
	 */
	protected HttpServletResponseMock mockHttpServletResponse() {
		
		//
		// Response
		//
		HttpServletResponseMock result = mock(HttpServletResponseMock.class, new DefaultAnswer());

		return result;
	}
	
	/**
	 * An implementation of Answer that overrides Mockito's defaults.
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

