package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.impl.StateServeEvent;
import com.variant.core.impl.StateServeEventTestFacade;
import com.variant.core.jdbc.EventReader;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.jdbc.VariantEventFromDatabase;
import com.variant.core.jdbc.VariantEventVariantFromDatabase;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

public class EventWriterTest extends BaseTest {
	
	@Before
	public void beforeEachTest() throws Exception {
		// Reboot the api with the postgres event persister: we'll be reading back.
		api.shutdown();
		api.bootstrap("/variant-junit.props", "/variant-junit-postgres.props");
		JdbcUtil.recreateSchema();
	}
	
	//@Test 
	// TODO
	public void performaceTest() throws Exception {

		
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		long timestamp = System.currentTimeMillis();
		Schema schema = api.getSchema();
		State state = schema.getState("state1");
		VariantSession ssn = api.getSession("ssn1");
		VariantStateRequest request = api.dispatchRequest(ssn, state, timestamp + ".state1.A");
		HashMap<String,String> params = new HashMap<String, String>() {{put("path", "viewResolvedPath");}};
		StateServeEventTestFacade event1 = new StateServeEventTestFacade(request, params);
		request = api.dispatchRequest(ssn, state, timestamp + ".state1.B");
		StateServeEventTestFacade event2 = new StateServeEventTestFacade(request, params);
		event1.setParameter("event1-key1", "value1");
		event2.setParameter("event2-key1", "value1");
		event2.setParameter("event2-key2", "value2"); 
		api.getEventWriter().write(event1);
		api.getEventWriter().write(event2);

		// Wait for the async writer thread to commit;
		Thread.sleep(2000);
		
		Collection<VariantEventFromDatabase> events = EventReader.readEvents();
		assertEquals(2, events.size());

	}
	
	@Test
	public void basicTest() throws Exception {

		ParserResponse response = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = api.getSchema();
		State s1 = schema.getState("state1");
		State s2 = schema.getState("state2");
		
		VariantSession ssn1 = api.getSession("ssn1");
		VariantStateRequest request = api.dispatchRequest(ssn1, s1, targetingTrackerString("test1.A","test2.B","test3.C","test4.A","test5.B","test6.C"));
		assertEquals(1, request.getPendingEvents().size());

		VariantSession ssn2 = api.getSession("ssn2");
		request = api.dispatchRequest(ssn2, s2, targetingTrackerString("test2.C","test3.A","test4.A","test5.B","test6.C"));

		api.commitStateRequest(ssn1.getStateRequest(), "");
		api.commitStateRequest(ssn2.getStateRequest(), "");

		// Wait for the async writer thread to commit;
		Thread.sleep(1000);
		
		Collection<VariantEventFromDatabase> events = EventReader.readEvents();
		assertEquals(2, events.size());

		for (VariantEventFromDatabase e: events) {
			assertEquals(StateServeEvent.EVENT_NAME, e.getEventName());
			if (e.getEventValue().equals("state1")) {
				assertEquals("ssn1", e.getSessionId());
				assertEquals("state1", e.getEventValue());
				assertEquals(2, e.getParameterMap().size());
				assertEquals("/path/to/state1/test5.B+test6.C", e.getParameterMap().get("PATH"));
				assertEquals("OK", e.getParameterMap().get("REQ_STATUS"));
				assertEquals(5, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test2")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
						assertTrue(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
						assertTrue(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else if (e.getEventValue().equals("state2")) {
				assertEquals("ssn2", e.getSessionId());
				assertEquals("state2", e.getEventValue());
				assertEquals(2, e.getParameterMap().size());
				assertEquals("/path/to/state2/test2.C+test5.B", e.getParameterMap().get("PATH"));
				assertEquals("OK", e.getParameterMap().get("REQ_STATUS"));
				assertEquals(6, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
						assertFalse(ee.isStateNonvariant());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
						assertTrue(ee.isStateNonvariant());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Not supposed to be here", false);
		}
	}

}
