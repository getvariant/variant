package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.StateNotInstrumentedException;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.VariantEvent;
import com.variant.core.exception.CommonError;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class StateRequestTest extends BaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	
	/**
	 */
	@org.junit.Test
	public void noStabilTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());

	   	Schema schema = conn.getSchema();
	   	State state1 = schema.getState("state1");
	   	final Test test1 = schema.getTest("test1");
	   	Test test2 = schema.getTest("test2");
	   	Test test3 = schema.getTest("test3");
	   	Test test4 = schema.getTest("test4");
	   	Test test5 = schema.getTest("test5");
	   	Test test6 = schema.getTest("test6");

	   	final StateRequest req = ssn.targetForState(state1);
	   	assertNotNull(req);
		assertEquals(5, req.getLiveExperiences().size());
		
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				req.getLiveExperience(test1);
			}
			@Override public void onThrown(ClientException.User e) {
				assertEquals(CommonError.STATE_NOT_INSTRUMENTED_BY_TEST, e.getError());
			}
		}.assertThrown(StateNotInstrumentedException.class);
		
		Experience e2 = req.getLiveExperience(test2);
		assertNotNull(e2);
		Experience e3 = req.getLiveExperience(test3);
		assertNotNull(e3);
		Experience e4 = req.getLiveExperience(test4);
		assertNotNull(e4);
		Experience e5 = req.getLiveExperience(test5);
		assertNotNull(e5);
		Experience e6 = req.getLiveExperience(test6);
		assertNotNull(e6);
	   	
		assertFalse(req.isCommitted());
		
		/* On occasion, we may get a trivial resolution and these will fail
		assertNotNull(req.getResolvedParameters().get("path"));
		assertNotNull(req.getResolvedStateVariant());
		*/

		assertEquals(ssn, req.getSession());
		assertEquals(state1, req.getState());
		VariantEvent event = req.getStateVisitedEvent();
		assertNotNull(event);
		assertEquals(StateVisitedEvent.EVENT_NAME, event.getName());
		assertEquals(req.getState().getName(), event.getValue());
		assertEquals(ssn.getCreateDate().getTime(), event.getCreateDate().getTime(), 10);
		assertTrue(event.getParameterMap().isEmpty());
		
		req.commit();
		
		conn.close();
	}
	
	/**
	 */
	//@org.junit.Test
	public void deterministicTest1() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());

	   	Schema schema = conn.getSchema();
	   	State state1 = schema.getState("state1");
	   	final Test test1 = schema.getTest("test1");
	   	Test test2 = schema.getTest("test2");
	   	Test test3 = schema.getTest("test3");
	   	Test test4 = schema.getTest("test4");
	   	Test test5 = schema.getTest("test5");
	   	Test test6 = schema.getTest("test6");

	   	final StateRequest req = ssn.targetForState(state1);
	   	assertNotNull(req);
		assertEquals(5, req.getLiveExperiences().size());
		
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				req.getLiveExperience(test1);
			}
			@Override public void onThrown(ClientException.User e) {
				assertEquals(CommonError.STATE_NOT_INSTRUMENTED_BY_TEST, e.getError());
			}
		}.assertThrown(StateNotInstrumentedException.class);
		
		Experience e2 = req.getLiveExperience(test2);
		assertNotNull(e2);
		Experience e3 = req.getLiveExperience(test3);
		assertNotNull(e3);
		Experience e4 = req.getLiveExperience(test4);
		assertNotNull(e4);
		Experience e5 = req.getLiveExperience(test5);
		assertNotNull(e5);
		Experience e6 = req.getLiveExperience(test6);
		assertNotNull(e6);
	   	
		//System.out.println(VariantStringUtils.toString(req.getLiveExperiences(), ", "));
		assertNotNull(req.getResolvedParameters().get("path"));
		
		assertNotNull(req.getResolvedStateVariant());
		
		
	}

	/**
	 */
	//@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		

		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
		assertFalse(ssn.isExpired());
	
	   	Schema schema = conn.getSchema();
	   	State state2 = schema.getState("state2");
//	   	Test test2 = schema.getTest("test1");
	   	final StateRequest req = ssn.targetForState(state2);
	   	
		assertEquals(1000, ssn.getTimeoutMillis());
		// Let vacuum thread a chance to run.
		Thread.sleep(2000);
		
		assertTrue(ssn.isExpired());
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				req.commit();
			}
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.SESSION_EXPIRED, e.getError());
			}
		}.assertThrown(SessionExpiredException.class);

	}
	
}
