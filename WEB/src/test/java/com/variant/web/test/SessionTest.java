package com.variant.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.impl.VariantCoreImplTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.web.mock.HttpServletResponseMock;

public class SessionTest extends BaseTestWeb {

	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSchemaTest() throws Exception {

		assertNull(webApi.getSchema());
		final HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);  // no vssn.
		final HttpServletResponse httpResp = mockHttpServletResponse();

		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { webApi.getSession(httpReq, httpResp); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_UNDEFINED);	
	}

	/**
	 * Old Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void oldSchemaTest() throws Exception {

		ParserResponse response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		final HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null); // no vssn.
		final HttpServletResponseMock httpResp = mockHttpServletResponse();

		final VariantSession ssn1 = webApi.getSession(httpReq, httpResp);
		final VariantCoreImplTestFacade coreFacade = new VariantCoreImplTestFacade(coreApi);
		coreFacade.getSessionService().saveSession(ssn1, httpReq);     // succeeds
		
		// replace the schema and the session save should fail.
		response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { coreFacade.getSessionService().saveSession(ssn1, httpReq); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_REPLACED);
	}

	/**
	 * No session ID in cookie.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSessionIDInTrackerTest() throws Exception {
		
		ParserResponse response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = webApi.getSchema();
		
		HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);  // no vssn.
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantSession ssn1 = webApi.getSession(httpReq, httpResp);
		assertNotNull(ssn1);
		assertNotNull(ssn1.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(1, httpResp.getCookies().length);
		
		VariantSession ssn2 = webApi.getSession(httpReq, httpResp);
		assertNotNull(ssn2);
		
		// getSession() should be idempotent.
		assertEquals(ssn1, ssn2);
		assertNull(ssn2.getStateRequest());		
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
		assertEquals(1, httpResp.getCookies().length);

		State state1 = schema.getState("state1");		
		VariantStateRequest varReq = webApi.dispatchRequest(ssn1, state1, httpReq);
		webApi.commitStateRequest(varReq, httpResp);
		
		// commit() has added the targeting tracker cookie.
		Cookie[] outgoingCookies = httpResp.getCookies();
		assertEquals(2, outgoingCookies.length);
		assertEquals(ssn1.getId(), outgoingCookies[0].getValue());
	}

	
}
