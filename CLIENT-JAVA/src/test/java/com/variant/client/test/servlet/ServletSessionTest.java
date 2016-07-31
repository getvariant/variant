package com.variant.client.test.servlet;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.impl.ClientStateRequestImpl;
import com.variant.client.mock.HttpServletResponseMock;
import com.variant.client.servlet.SessionIdTrackerHttpCookie;
import com.variant.client.servlet.TargetingTrackerHttpCookie;
import com.variant.client.servlet.VariantServletClient;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantStringUtils;

public class ServletSessionTest extends ServletAdapterBaseTest {

	VariantServletClient client = newServletAdapterClient();
	
	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSchemaTest() throws Exception {

		assertNull(client.getSchema());
		final HttpServletRequest httpReq = mockHttpServletRequest("foo", (String)null);  // no vssn.

		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { client.getSession(httpReq); }
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
		
		final HttpServletRequest httpReq = mockHttpServletRequest("foo", (String)null);
		final Schema oldSchema = client.getSchema();

		final VariantSession ssn1 = client.getSession(httpReq);
		
		// replace the schema and all ops on ssn1 should fail.
		response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		final Schema newSchema = client.getSchema();
		
		// Can't obtain state on an obsolete schema.
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { 
				 oldSchema.getState("state1");
			}
		}.assertThrown(MessageTemplate.RUN_SCHEMA_OBSOLETE, oldSchema.getId());

		// Can't target a session created with old schema.
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { 
				ssn1.targetForState(newSchema.getState("state1"));
			}
		}.assertThrown(MessageTemplate.RUN_SCHEMA_MODIFIED, client.getSchema().getId(), oldSchema.getId());

		// Get a new session, target it and replace schema again.
		
		VariantSession ssn2 = client.getSession(httpReq);
		final VariantStateRequest req = ssn2.targetForState(newSchema.getState("state1"));

		response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		final Schema newSchema2 = client.getSchema();
		final HttpServletResponseMock httpResp = mockHttpServletResponse();
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { 
				req.commit(httpResp);
			}
		}.assertThrown(MessageTemplate.RUN_SCHEMA_MODIFIED, newSchema2.getId(), newSchema.getId());
	
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
		
		HttpServletRequest httpReq = mockHttpServletRequest(null, (String)null);
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantSession ssn1 = client.getSession(httpReq);
		assertNotNull(ssn1);
		assertNotNull(ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(0, httpResp.getCookies().length);
		//assertEquals(1, httpResp.getCookies().length);  Cookie should be added after commit;
		
		VariantSession ssn2 = client.getSession(httpReq);
		assertNotNull(ssn2);
		
		// No guarantee that consequtive calls to getSession() with the same args will return the same object.
		//assertEquals(ssn1, ssn2); 
		assertNull(ssn2.getStateRequest());		
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());

		State state1 = schema.getState("state1");		
		VariantStateRequest varReq = ssn2.targetForState(state1);
		//System.out.println(((VariantSessionImpl)ssn2).toJson());
		assertEquals(state1, varReq.getState());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((ClientStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ClientStateRequestImpl)ssn2.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		
		Collection<Test> expectedTests = VariantCollectionsUtils.list(
				schema.getTest("test2"), 
				schema.getTest("test3"), 
				schema.getTest("test4"), 
				schema.getTest("test5"), 
				schema.getTest("test6"));
		
		assertEqualAsSets(expectedTests, ssn2.getTraversedTests());

		varReq.commit(httpResp);

		// commit() has added the targeting tracker cookie.
		assertEquals(2, httpResp.getCookies().length);
		assertEquals(ssn2, varReq.getSession());
		assertEquals(ssn2.getId(), httpResp.getCookie(SessionIdTrackerHttpCookie.COOKIE_NAME).getValue());
		for (Test test: expectedTests)
			assertMatches(".*\\." + test.getName() + "\\..*", httpResp.getCookie(TargetingTrackerHttpCookie.COOKIE_NAME).getValue());
		
		// The session shouldn't have changed after commit.
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((ClientStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ClientStateRequestImpl)ssn2.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));

		assertEqualAsSets(expectedTests, ssn2.getTraversedTests());

		// Commit should have saved the session.
		httpReq = mockHttpServletRequest(httpResp);
		VariantSession ssn3 = client.getSession(httpReq);
		assertEquals(ssn3, ssn2);
		assertEquals(ssn3.getSchemaId(), schema.getId());
		System.out.println(((ClientStateRequestImpl)varReq).getCoreStateRequest().toJson());
		System.out.println(((ClientStateRequestImpl)ssn3.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				((ClientStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ClientStateRequestImpl)ssn3.getStateRequest()).getCoreStateRequest().toJson());
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
		HttpServletRequest httpReq = mockHttpServletRequest(sessionId, (String)null);
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantSession ssn1 = client.getSession(httpReq);
		assertNotNull(ssn1);
		assertEquals(sessionId, ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(0, httpResp.getCookies().length);  // We didn't drop the ssnid cookie, because there was one in request.
		
		State state2 = schema.getState("state2");		
		VariantStateRequest varReq = ssn1.targetForState(state2);
		assertEquals(ssn1, varReq.getSession());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertEquals(
				((ClientStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ClientStateRequestImpl)ssn1.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn1.getTraversedStates().toArray()));
		
		Collection<Test> expectedTests = VariantCollectionsUtils.list(
				schema.getTest("test1"), 
				schema.getTest("test2"), 
				schema.getTest("test3"), 
				schema.getTest("test4"), 
				schema.getTest("test5"), 
				schema.getTest("test6"));

		assertEqualAsSets(expectedTests, ssn1.getTraversedTests());		

		varReq.commit(httpResp);
		
		// Create a new HTTP request with the same VRNT-SSNID cookie.  Should fetch the same session.
		HttpServletRequest httpReq2 = mockHttpServletRequest(sessionId, (String)null);
		VariantSession ssn2 = client.getSession(httpReq2);
		assertEquals(ssn2, varReq.getSession());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(ssn2.getStateRequest().getResolvedParameterMap(), varReq.getSession().getStateRequest().getResolvedParameterMap());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		assertEqualAsSets(expectedTests, ssn1.getTraversedTests());		
		
		
	}
}
