package com.variant.client.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.mockito.Answers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.variant.client.VariantClient;
import com.variant.client.VariantClientTestFacade;
import com.variant.client.mock.HttpServletRequestMock;
import com.variant.client.mock.HttpServletResponseMock;
import com.variant.client.mock.HttpSessionMock;
import com.variant.client.servlet.adapter.SessionIdTrackerHttpCookie;
import com.variant.core.impl.VariantCore;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;
import com.variant.core.test.BaseTestCommon;

/**
 * Base class for all Core JUnit tests.
 */
public abstract class BaseTestClient extends BaseTestCommon {
	
	private static Boolean sqlSchemaCreated = false;

	protected VariantClient client = null;
	protected VariantCore coreApi = null;
	
	/**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 */
	@Before
	public void _beforeTestCase() throws Exception {
		client = rebootApi();               // in each instance 
		coreApi = VariantClientTestFacade.getCoreApi(client);
		
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
	protected VariantClient rebootApi() {
		return new VariantClient("/variant-test.props");
	}

	@Override
	protected JdbcService getJdbcService() {
		return new JdbcService(coreApi);
	}

	@Override
	protected Schema getSchema() {
		return client.getSchema();
	}

	//---------------------------------------------------------------------------------------------//
	//                                      Mockito Mocks                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Mock HttpServletRequest
	 * @return
	 */
	protected HttpServletRequestMock mockHttpServletRequest(String jsessionId, String vsessionId) { 
		
		//
		// Session
		//
		HttpSessionMock ssn = mock(HttpSessionMock.class, new DefaultAnswer());
		when(ssn.getId()).thenReturn(jsessionId);

		// Request
		HttpServletRequestMock result = mock(HttpServletRequestMock.class, new DefaultAnswer());
		when(result.getSession()).thenReturn(ssn);
		Cookie[] cookies = { new Cookie(SessionIdTrackerHttpCookie.COOKIE_NAME, vsessionId) };
		when(result.getCookies()).thenReturn(cookies);
		
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

