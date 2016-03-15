package com.variant.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.impl.VariantCoreImplTestFacade;
import com.variant.core.impl.VariantStateRequestImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantStringUtils;
import com.variant.web.mock.HttpServletResponseMock;
import com.variant.web.util.VariantWebStateRequestDecorator;

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
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(1, httpResp.getCookies().length);
		
		VariantSession ssn2 = webApi.getSession(httpReq, httpResp);
		assertNotNull(ssn2);
		
		// getSession() should be idempotent.
		assertEquals(ssn1, ssn2);
		assertNull(ssn2.getStateRequest());		
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
		assertEquals(1, httpResp.getCookies().length);

		State state1 = schema.getState("state1");		
		VariantStateRequest varReq = webApi.dispatchRequest(ssn2, state1, httpReq);
		assertEquals(state1, varReq.getState());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((VariantWebStateRequestDecorator)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn2.getStateRequest()).toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		assertEquals(
				"[(test6, true), (test4, true), (test5, true), (test2, true), (test3, true)]", 
				Arrays.toString(ssn2.getTraversedTests().toArray()));

		webApi.commitStateRequest(varReq, httpResp);

		// commit() has added the targeting tracker cookie.
		Cookie[] outgoingCookies = httpResp.getCookies();
		assertEquals(2, outgoingCookies.length);
		assertEquals(ssn1.getId(), outgoingCookies[0].getValue());
		
		// The session shouldn't have changed after commit.
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((VariantWebStateRequestDecorator)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn2.getStateRequest()).toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		assertEquals(
				"[(test6, true), (test4, true), (test5, true), (test2, true), (test3, true)]", 
				Arrays.toString(ssn2.getTraversedTests().toArray()));

		// Commit should have saved the session.
		VariantSession ssn3 = webApi.getSession(httpReq, httpResp);
		assertEquals(ssn3, ssn2);
		assertEquals(ssn3.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((VariantWebStateRequestDecorator)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn3.getStateRequest()).toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn3.getTraversedStates().toArray()));
		assertEquals(
				"[(test6, true), (test4, true), (test5, true), (test2, true), (test3, true)]", 
				Arrays.toString(ssn3.getTraversedTests().toArray()));		
	}
	
	/**
	 * Session ID in cookie.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void sessionIDInTrackerTest() throws Exception {
		
		ParserResponse response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = webApi.getSchema();
		String sessionId = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));
		HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", sessionId);
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantSession ssn1 = webApi.getSession(httpReq, httpResp);
		assertNotNull(ssn1);
		assertEquals(sessionId, ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(0, httpResp.getCookies().length);  // We didn't drop the ssnid cookie, because there was one in request.
		
		State state2 = schema.getState("state2");		
		VariantStateRequest varReq = webApi.dispatchRequest(ssn1, state2, httpReq);
		assertEquals(ssn1, varReq.getSession());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((VariantWebStateRequestDecorator)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn1.getStateRequest()).toJson());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn1.getTraversedStates().toArray()));
		assertEquals(
				"[(test1, true), (test6, true), (test4, true), (test5, true), (test2, true), (test3, true)]", 
				Arrays.toString(ssn1.getTraversedTests().toArray()));		

		webApi.commitStateRequest(varReq, httpResp);
		
		// Create a new HTTP request with the same VRNT-SSNID cookie.  Should fetch the same session.
		// but without the state request.
		HttpServletRequest httpReq2 = mockHttpServletRequest("JSESSIONID", sessionId);
		HttpServletResponseMock httpResp2 = mockHttpServletResponse();
		VariantSession ssn2 = webApi.getSession(httpReq2, httpResp2);
		assertEquals(ssn2, varReq.getSession());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertNull(ssn2.getStateRequest());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		assertEquals(
				"[(test1, true), (test6, true), (test4, true), (test5, true), (test2, true), (test3, true)]", 
				Arrays.toString(ssn1.getTraversedTests().toArray()));		
		
		
	}
}
