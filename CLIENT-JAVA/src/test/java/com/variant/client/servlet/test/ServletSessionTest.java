package com.variant.client.servlet.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.mock.HttpServletResponseMock;
import com.variant.client.servlet.SessionIdTrackerHttpCookie;
import com.variant.client.servlet.TargetingTrackerHttpCookie;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.VariantServletSession;
import com.variant.client.servlet.VariantServletStateRequest;
import com.variant.client.servlet.impl.ServletSessionImpl;
import com.variant.client.servlet.impl.ServletStateRequestImpl;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantStringUtils;

public class ServletSessionTest extends ServletClientBaseTest {

	private static Random rand = new Random(System.currentTimeMillis());
	private VariantServletClient servletClient = newServletAdapterClient();
	
	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSchemaTest() throws Exception {

		assertNull(servletClient.getSchema());
		final HttpServletRequest httpReq = mockHttpServletRequest("foo", (String)null);  // no vssn.

		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { servletClient.getOrCreateSession(httpReq); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_UNDEFINED);	
	}

	/**
	 * Old Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void oldSchemaTest() throws Exception {

		ParserResponse response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		final HttpServletRequest httpReq = mockHttpServletRequest("foo", (String)null);
		final Schema oldSchema = servletClient.getSchema();

		final VariantSession ssn1 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn1);
		
		// replace the schema and all ops on ssn1 should fail.
		response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		final Schema newSchema = servletClient.getSchema();
		
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
		}.assertThrown(MessageTemplate.RUN_SCHEMA_MODIFIED, servletClient.getSchema().getId(), oldSchema.getId());

		// Get a new session, target it and replace schema again.
		
		VariantSession ssn2 = servletClient.getOrCreateSession(httpReq);
		final VariantStateRequest req = ssn2.targetForState(newSchema.getState("state1"));

		response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		final Schema newSchema2 = servletClient.getSchema();
		final HttpServletResponseMock httpResp = mockHttpServletResponse();
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { 
				req.commit(httpResp);
			}
		}.assertThrown(MessageTemplate.RUN_SCHEMA_MODIFIED, newSchema2.getId(), newSchema.getId());
	
	}

	
	/**
	 * Test bare and servlet signatures of getSession()
	 * for the case when there's no session ID in the tracker.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void getSessionNoTrackerTest() throws Exception {

		ParserResponse response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		// Servlet signatures
		final HttpServletRequest httpReq = mockHttpServletRequest(null, (String)null);
		
		VariantSession ssn1 = servletClient.getSession(httpReq);
		assertNull(ssn1);
		
		ssn1 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn1);

		VariantSession ssn2 = servletClient.getSession(httpReq);
		assertNull(ssn2);
		
		ssn2 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn2);
		assertNotEquals(ssn1, ssn2);

		// Bare signatures		
		ssn1 = servletClient.getSession((Object) httpReq);
		assertNull(ssn1);
		
		ssn1 = servletClient.getOrCreateSession((Object)httpReq);
		assertNotNull(ssn1);

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getSession(new Object());
			}
		}.assertThrown("Invalid user data: HttpServletRequest expected");

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getSession(httpReq, new Object());
			}
		}.assertThrown("Invalid user data: single element vararg expected");

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getOrCreateSession(new Object());
			}
		}.assertThrown("Invalid user data: HttpServletRequest expected");

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getOrCreateSession(httpReq, new Object());
			}
		}.assertThrown("Invalid user data: single element vararg expected");

	}
	
	/**
	 * Test bare and servlet signatures of getSession()
	 * for the case when is a session ID in the tracker.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void getSessionWithTrackerTest() throws Exception {

		ParserResponse response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		// Servlet signatures
		String sessionId = VariantStringUtils.random64BitString(rand);
		final HttpServletRequest httpReq = mockHttpServletRequest(sessionId, (String)null);
		
		VariantSession ssn1 = servletClient.getSession(httpReq);
		assertNull(ssn1);
		
		ssn1 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn1);

		VariantSession ssn2 = servletClient.getSession(httpReq);
		assertNotNull(ssn2);  // ID in tracker => session already created by previous call.
		
		ssn2 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn2);
		assertEquals(((ServletSessionImpl)ssn1).getBareSession(), ((ServletSessionImpl)ssn2).getBareSession());

		// Bare signatures		
		sessionId = VariantStringUtils.random64BitString(rand);
		final HttpServletRequest httpReq2 = mockHttpServletRequest(sessionId, (String)null);

		ssn1 = servletClient.getSession((Object) httpReq2);
		assertNull(ssn1);
		
		ssn1 = servletClient.getOrCreateSession((Object)httpReq2);
		assertNotNull(ssn1);

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getSession(new Object());
			}
		}.assertThrown("Invalid user data: HttpServletRequest expected");

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getSession(httpReq2, new Object());
			}
		}.assertThrown("Invalid user data: single element vararg expected");

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getOrCreateSession(new Object());
			}
		}.assertThrown("Invalid user data: HttpServletRequest expected");

		new IllegalArgumentExceptionInterceptor() {
			@Override public void toRun() { 
				servletClient.getOrCreateSession(httpReq2, new Object());
			}
		}.assertThrown("Invalid user data: single element vararg expected");

	}
	
	/**
	 * No session ID in cookie.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSessionWithIdInTrackerTest() throws Exception {
		
		ParserResponse response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = servletClient.getSchema();
		
		HttpServletRequest httpReq = mockHttpServletRequest(null, (String)null);
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantServletSession ssn1 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn1);
		assertNotNull(ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(0, httpResp.getCookies().length);
		//assertEquals(1, httpResp.getCookies().length);  Cookie should be added after commit;
		
		VariantServletSession ssn2 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn2);
		
		// No guarantee that consequtive calls to getSession() with the same args will return the same object.
		//assertEquals(ssn1, ssn2); 
		assertNull(ssn2.getStateRequest());		
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());

		State state1 = schema.getState("state1");		
		VariantServletStateRequest varReq = ssn2.targetForState(state1);
		//System.out.println(((VariantSessionImpl)ssn2).toJson());
		assertEquals(state1, varReq.getState());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((ServletStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ServletStateRequestImpl)ssn2.getStateRequest()).getCoreStateRequest().toJson());
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
				((ServletStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ServletStateRequestImpl)ssn2.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));

		assertEqualAsSets(expectedTests, ssn2.getTraversedTests());

		// Commit should have saved the session.
		httpReq = mockHttpServletRequest(httpResp);
		VariantSession ssn3 = servletClient.getSession(httpReq);
		assertEquals(ssn3, ssn2);
		assertEquals(ssn3.getSchemaId(), schema.getId());
		System.out.println(((ServletStateRequestImpl)varReq).getCoreStateRequest().toJson());
		System.out.println(((ServletStateRequestImpl)ssn3.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				((ServletStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ServletStateRequestImpl)ssn3.getStateRequest()).getCoreStateRequest().toJson());
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
		
		ParserResponse response = servletClient.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = servletClient.getSchema();
		String sessionId = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));
		HttpServletRequest httpReq = mockHttpServletRequest(sessionId, (String)null);
		HttpServletResponseMock httpResp = mockHttpServletResponse();

		VariantSession ssn1 = servletClient.getSession(httpReq);
		assertNull(ssn1);

		ssn1 = servletClient.getOrCreateSession(httpReq);
		assertNotNull(ssn1);
		assertEquals(sessionId, ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		assertEquals(0, httpResp.getCookies().length);  // We didn't drop the ssnid cookie, because there was one in request.
		
		State state2 = schema.getState("state2");		
		VariantStateRequest varReq = ssn1.targetForState(state2);
		assertEquals(((ServletSessionImpl)ssn1).getBareSession(), ((ServletSessionImpl)varReq.getSession()).getBareSession());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertEquals(
				((ServletStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((ServletStateRequestImpl)ssn1.getStateRequest()).getCoreStateRequest().toJson());
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
		
		// Create a new HTTP request with the same VRNT-SSNID cookie.  Should fetch the same bare session.
		HttpServletRequest httpReq2 = mockHttpServletRequest(sessionId, (String)null);
		VariantSession ssn2 = servletClient.getSession(httpReq2);
		assertEquals(((ServletSessionImpl)ssn2).getBareSession(), ((ServletSessionImpl)varReq.getSession()).getBareSession());
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(ssn2.getStateRequest().getResolvedParameterMap(), varReq.getSession().getStateRequest().getResolvedParameterMap());
		assertEquals(
				"[(state2, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));
		assertEqualAsSets(expectedTests, ssn1.getTraversedTests());		
		
		
	}
}
