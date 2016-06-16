package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.client.impl.ClientStateRequestWrapper;
import com.variant.client.impl.VariantClientSession;
import com.variant.client.mock.HttpServletResponseMock;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.impl.VariantStateRequestImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantStringUtils;

public class SessionTest extends BaseTestClient {

	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSchemaTest() throws Exception {

		assertNull(client.getSchema());
		final HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);  // no vssn.
		final HttpServletResponse httpResp = mockHttpServletResponse();

		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { client.getSession(httpReq, httpResp); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_UNDEFINED);	
	}

	/**
	 * Old Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void oldSchemaTest() throws Exception {

		ParserResponse response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		final HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null); // no vssn.
		final HttpServletResponseMock httpResp = mockHttpServletResponse();

		final VariantCoreSession ssn1 = client.getSession(httpReq, httpResp);
		coreApi.getSessionService().saveSession(((VariantClientSession)ssn1).getCoreSession(), httpReq);     // succeeds
		String oldSchemaId = client.getSchema().getId();
		
		// replace the schema and the session save should fail.
		response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { coreApi.getSessionService().saveSession(((VariantClientSession)ssn1).getCoreSession(), httpReq); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_REPLACED, getSchema().getId(), oldSchemaId);
	}

	/**
	 * No session ID in cookie.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSessionIDInTrackerTest() throws Exception {
		
		ParserResponse response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = client.getSchema();
		
		HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);  // no vssn.
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantCoreSession ssn1 = client.getSession(httpReq, httpResp);
		assertNotNull(ssn1);
		assertNotNull(ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(1, httpResp.getCookies().length);
		
		VariantCoreSession ssn2 = client.getSession(httpReq, httpResp);
		assertNotNull(ssn2);
		
		// getSession() should be idempotent.
		assertEquals(ssn1, ssn2);
		assertNull(ssn2.getStateRequest());		
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
		assertEquals(1, httpResp.getCookies().length);

		State state1 = schema.getState("state1");		
		VariantStateRequest varReq = ssn2.targetForState(state1, httpReq);
		//System.out.println(((VariantSessionImpl)ssn2).toJson());
		assertEquals(state1, varReq.getState());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((ClientStateRequestWrapper)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn2.getStateRequest()).toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		
		Collection<Pair<Test,Boolean>> expectedTests = VariantCollectionsUtils.list(
				new Pair<Test,Boolean>(schema.getTest("test2"), true), 
				new Pair<Test,Boolean>(schema.getTest("test3"), true), 
				new Pair<Test,Boolean>(schema.getTest("test4"), true), 
				new Pair<Test,Boolean>(schema.getTest("test5"), true), 
				new Pair<Test,Boolean>(schema.getTest("test6"), true));
		
		assertEqualAsSets(expectedTests, ssn2.getTraversedTests());

		varReq.commit(httpReq, httpResp);

		// commit() has added the targeting tracker cookie.
		Cookie[] outgoingCookies = httpResp.getCookies();
		assertEquals(2, outgoingCookies.length);
		assertEquals(ssn1.getId(), outgoingCookies[0].getValue());
		
		// The session shouldn't have changed after commit.
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((ClientStateRequestWrapper)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn2.getStateRequest()).toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));

		assertEqualAsSets(expectedTests, ssn2.getTraversedTests());

		// Commit should have saved the session.
		VariantCoreSession ssn3 = client.getSession(httpReq, httpResp);
		assertEquals(ssn3, ssn2);
		assertEquals(ssn3.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((ClientStateRequestWrapper)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn3.getStateRequest()).toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn3.getTraversedStates().toArray()));
		assertEqualAsSets(expectedTests, ssn3.getTraversedTests());		
	}
	
	/**
	 * Session ID in cookie.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void sessionIDInTrackerTest() throws Exception {
		
		ParserResponse response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = client.getSchema();
		String sessionId = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));
		HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", sessionId);
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantCoreSession ssn1 = client.getSession(httpReq, httpResp);
		assertNotNull(ssn1);
		assertEquals(sessionId, ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(0, httpResp.getCookies().length);  // We didn't drop the ssnid cookie, because there was one in request.
		
		State state2 = schema.getState("state2");		
		VariantStateRequest varReq = ssn1.targetForState(state2, httpReq);
		assertEquals(ssn1, varReq.getSession());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)((ClientStateRequestWrapper)varReq).getOriginalRequest()).toJson(), 
				((VariantStateRequestImpl)ssn1.getStateRequest()).toJson());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn1.getTraversedStates().toArray()));
		
		Collection<Pair<Test,Boolean>> expectedTests = VariantCollectionsUtils.list(
				new Pair<Test,Boolean>(schema.getTest("test1"), true), 
				new Pair<Test,Boolean>(schema.getTest("test2"), true), 
				new Pair<Test,Boolean>(schema.getTest("test3"), true), 
				new Pair<Test,Boolean>(schema.getTest("test4"), true), 
				new Pair<Test,Boolean>(schema.getTest("test5"), true), 
				new Pair<Test,Boolean>(schema.getTest("test6"), true));

		assertEqualAsSets(expectedTests, ssn1.getTraversedTests());		

		varReq.commit(httpReq, httpResp);
		
		// Create a new HTTP request with the same VRNT-SSNID cookie.  Should fetch the same session.
		HttpServletRequest httpReq2 = mockHttpServletRequest("JSESSIONID", sessionId);
		HttpServletResponseMock httpResp2 = mockHttpServletResponse();
		VariantCoreSession ssn2 = client.getSession(httpReq2, httpResp2);
		assertEquals(ssn2, varReq.getSession());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(ssn2.getStateRequest().getResolvedParameterMap(), varReq.getSession().getStateRequest().getResolvedParameterMap());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		assertEqualAsSets(expectedTests, ssn1.getTraversedTests());		
		
		
	}
}
