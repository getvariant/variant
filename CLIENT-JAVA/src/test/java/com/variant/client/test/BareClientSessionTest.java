package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.variant.client.VariantClient;
import com.variant.client.impl.ClientStateRequestImpl;
import com.variant.client.session.ClientSessionImpl;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantStringUtils;

public class BareClientSessionTest extends BareClientBaseTest {

	private final VariantClient client = newBareClient();
	private final Random random = new Random(System.currentTimeMillis());
	
	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSchemaTest() throws Exception {
		
		assertNull(client.getSchema());

		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { 
				Object[] userData = userDataForSimpleIn(client.getSchema(), "foo");
				client.getOrCreateSession(userData); 
			}
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

		String oldSchemaId = client.getSchema().getId();
		final VariantSession ssn1 = client.getOrCreateSession(userDataForSimpleIn(client.getSchema(), "foo"));
		VariantStateRequest req = ssn1.targetForState(client.getSchema().getState("state1"));
		req.commit("");
		
		// replace the schema and the session save should fail.
		response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		new VariantRuntimeExceptionInterceptor() {
			@Override public void toRun() { 
				ssn1.targetForState(client.getSchema().getState("state1"));
			}
		}.assertThrown(MessageTemplate.RUN_SCHEMA_MODIFIED, client.getSchema().getId(), oldSchemaId);
	}

	/**
	 * No session ID in cookie.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		ParserResponse response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		Schema schema = client.getSchema();
		
		VariantSession ssn1 = client.getSession("foo");
		assertNull(ssn1);
		ssn1 = client.getOrCreateSession("foo");
		assertNotNull(ssn1);

		assertNotNull(ssn1.getId());
		assertEquals(ssn1.getSchemaId(), schema.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		
		VariantSession ssn2 = client.getSession("foo");
		assertNotNull(ssn2);
		
		assertEquals(ssn1, ssn2);
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

		varReq.commit("");

		assertEquals(5, ((ClientSessionImpl)ssn2).getTargetingTracker().get().size());
		assertEquals(5, varReq.getActiveExperiences().size());
		for (Test expectedTest: expectedTests) {
			assertNotNull(varReq.getActiveExperience(expectedTest));
		}
		
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
		VariantSession ssn3 = client.getSession("foo");
		assertEquals(ssn3, ssn2);
		assertEquals(ssn3.getSchemaId(), schema.getId());
		assertTrue(varReq.isCommitted());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn3.getTraversedStates().toArray()));
		assertEqualAsSets(expectedTests, ssn3.getTraversedTests());		
	}
	
	/**
	 * set/get session attributes.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void sessionAttributesTest() throws Exception {
		
		ParserResponse response = client.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());

		String sessionId = VariantStringUtils.random64BitString(random);

		VariantSession ssn1 = client.getOrCreateSession(sessionId);
		assertNotNull(ssn1);
		ssn1.setAttribute("23", 23);
		assertEquals(23, ssn1.getAttribute("23"));
		State state2 = client.getSchema().getState("state2");		
		VariantStateRequest varReq = ssn1.targetForState(state2);
		assertEquals(23, varReq.getSession().getAttribute("23"));
		ssn1.setAttribute("45", 45);
		varReq.commit("");
		VariantSession ssn2 = client.getSession(sessionId);
		assertEquals(ssn1, ssn2);
		assertEquals(23, ssn2.getAttribute("23"));
		assertEquals(45, ssn2.getAttribute("45"));
	}

}
