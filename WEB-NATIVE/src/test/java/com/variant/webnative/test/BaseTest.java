package com.variant.webnative.test;

import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.mockito.Mockito;

import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;
import com.variant.core.test.BaseTestCommon;
import com.variant.core.util.VariantStringUtils;
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

	protected HttpServletRequest mockHttpServletRequest() { 
		
		// Session
		String sessionId = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));

		HttpSession ssn = Mockito.mock(HttpSession.class);
		Mockito.when(ssn.getId()).thenReturn(sessionId);
		
		// Request
		HttpServletRequest result = Mockito.mock(HttpServletRequest.class);
		Mockito.when(result.getSession()).thenReturn(ssn);
		Mockito.when(result.getCookies()).thenReturn(new Cookie[0]);
		
		return result;
	}

}
