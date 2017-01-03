package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.Connection.Status;
import com.variant.client.impl.SessionImpl;
import com.variant.client.impl.StateRequestImpl;
import com.variant.core.UserError;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.CoreSession;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantStringUtils;

public class SessionTest extends BaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	private final Random random = new Random(System.currentTimeMillis());
	
	/**
	 * No session ID in tracker.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		String sessionId = VariantStringUtils.random64BitString(random);
		Session ssn = conn.getSession(sessionId);
		assertNull(ssn);
/*		
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
				((VariantStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((VariantStateRequestImpl)ssn2.getStateRequest()).getCoreStateRequest().toJson());
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

		assertEquals(5, ((VariantSessionImpl)ssn2).getTargetingTracker().get().size());
		assertEquals(5, varReq.getLiveExperiences().size());
		for (Test expectedTest: expectedTests) {
			assertNotNull(varReq.getLiveExperience(expectedTest));
		}
		
		// The session shouldn't have changed after commit.
		assertEquals(ssn2.getSchemaId(), schema.getId());
		assertEquals(
				((VariantStateRequestImpl)varReq).getCoreStateRequest().toJson(), 
				((VariantStateRequestImpl)ssn2.getStateRequest()).getCoreStateRequest().toJson());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn2.getTraversedStates().toArray()));

		assertEqualAsSets(expectedTests, ssn2.getTraversedTests());

		// Commit should have saved the session.
		CoreSession ssn3 = client.getSession("foo");
		assertEquals(ssn3, ssn2);
		assertEquals(ssn3.getSchemaId(), schema.getId());
		assertTrue(varReq.isCommitted());
		assertEquals(
				"[(state1, 1)]", 
				Arrays.toString(ssn3.getTraversedStates().toArray()));
		assertEqualAsSets(expectedTests, ssn3.getTraversedTests());		
*/
	}
	
	/**
	 * set/get session attributes.
	 * 
	 * @throws Exception
	 *
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
		CoreSession ssn2 = client.getSession(sessionId);
		assertEquals(ssn1, ssn2);
		assertEquals(23, ssn2.getAttribute("23"));
		assertEquals(45, ssn2.getAttribute("45"));
	}
*/
	
}
