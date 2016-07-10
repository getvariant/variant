package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.Predicate;
import org.junit.Test;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.StateVisitedEvent;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.impl.VariantCore;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.test.jdbc.EventExperienceFromDatabase;
import com.variant.core.test.jdbc.EventReader;
import com.variant.core.test.jdbc.VariantEventFromDatabase;

public class EventWriterTest extends BaseTestCore {
		

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void stateVisitedEventTest() throws Exception {

		VariantCore core = rebootApi();
		
		ParserResponse response = core.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		State s1 = schema.getState("state1");
		State s2 = schema.getState("state2");
		
		VariantCoreSession ssn1 = core.getSession("ssn1");
		// test5.B will be dropped as incompatible, T1 will be control because it's uninstrumented,
		// T2, T3 will be controls because they're nonvariant, hence we will expect toresolve totest6.C.
		setTargetingStabile(ssn1,"test1.A","test2.B","test3.C","test4.A","test5.B","test6.C");
		VariantCoreStateRequest request = ssn1.targetForState(s1);
		assertNotNull(request.getStateVisitedEvent());

		VariantCoreSession ssn2 = core.getSession("ssn2");
		setTargetingStabile(ssn2, "test2.C","test3.A","test4.A","test5.B","test6.C");
		request = ssn2.targetForState(s2);
		assertNotNull(request.getStateVisitedEvent());

		ssn1.getStateRequest().commit();
		ssn2.getStateRequest().commit();

		// Wait for the async writer thread to commit;
		Thread.sleep(500);
		
		// Read only events generated by this test.
		Collection<VariantEventFromDatabase> events = new EventReader(core).readEvents(
				new Predicate<VariantEventFromDatabase>() {
					@Override
					public boolean evaluate(VariantEventFromDatabase e) {
						return "ssn1".equals(e.getSessionId()) || "ssn2".equals(e.getSessionId());
					}
				});
		
		assertEquals(2, events.size());

		for (VariantEventFromDatabase e: events) {
			assertEquals(StateVisitedEvent.EVENT_NAME, e.getEventName());
			if (e.getEventValue().equals("state1")) {
				assertEquals("ssn1", e.getSessionId());
				assertEquals("state1", e.getEventValue());
				assertEquals(2, e.getParameterMap().size());
				assertEquals("/path/to/state1/test6.C", e.getParameterMap().get("PATH"));
				assertEquals("OK", e.getParameterMap().get("REQ_STATUS"));
				assertEquals(5, e.getEventExperiences().size());
				for (EventExperienceFromDatabase ee: e.getEventExperiences()) {
					if (ee.getTestName().equals("test2")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
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
				assertEquals(6, e.getEventExperiences().size());
				for (EventExperienceFromDatabase ee: e.getEventExperiences()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Unexpected event value [" + e.getEventValue() + "]", false);
		}

		new JdbcService(core).recreateSchema();

		core.finalize();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void customEventTest1() throws Exception {
		
		VariantCore core = rebootApi();
		
		ParserResponse response = core.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		State s2 = schema.getState("state2");
		
		VariantCoreSession ssn3 = core.getSession("ssn3");
		setTargetingStabile(ssn3, "test2.C","test3.A","test4.A","test5.B","test6.C");
		VariantCoreStateRequest request = ssn3.targetForState(s2);
		VariantEvent customEvent = new CustomEvent("foo", "bar");
		customEvent.getParameterMap().put("param-key", "param-value");
		ssn3.triggerEvent(customEvent);
		assertNotNull(request.getStateVisitedEvent());

		ssn3.getStateRequest().commit();

		// Wait for the async writer thread to commit;
		Thread.sleep(500);
		
		// Read only events generated by this test.
		Collection<VariantEventFromDatabase> events = new EventReader(core).readEvents(
				new Predicate<VariantEventFromDatabase>() {
					@Override
					public boolean evaluate(VariantEventFromDatabase e) {
						return "ssn3".equals(e.getSessionId());
					}
				});
		assertEquals(2, events.size());
		
		for (VariantEventFromDatabase e: events) {

			if (e.getEventValue().equals("bar")) {
				assertEquals("foo", e.getEventName());
				assertEquals("ssn3", e.getSessionId());
				assertEquals("bar", e.getEventValue());
				assertEquals(1, e.getParameterMap().size());
				assertEquals("param-value", e.getParameterMap().get("param-key"));
				assertEquals(6, e.getEventExperiences().size());
				for (EventExperienceFromDatabase ee: e.getEventExperiences()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else if (e.getEventValue().equals("state2")) {
				assertEquals(StateVisitedEvent.EVENT_NAME, e.getEventName());
				assertEquals("ssn3", e.getSessionId());
				assertEquals("state2", e.getEventValue());
				assertEquals(2, e.getParameterMap().size());
				assertEquals("/path/to/state2/test2.C+test5.B", e.getParameterMap().get("PATH"));
				assertEquals("OK", e.getParameterMap().get("REQ_STATUS"));
				assertEquals(6, e.getEventExperiences().size());
				for (EventExperienceFromDatabase ee: e.getEventExperiences()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Unexpected event value [" + e.getEventValue() + "]", false);
		}
		new JdbcService(core).recreateSchema();
		core.finalize();
	}
	
	/**
	 * Same thing but test2 and test3 are disqualified.
	 * @throws Exception
	 */
	@Test
	public void customEventTest2() throws Exception {

		VariantCore core = rebootApi();
		
		ParserResponse response = core.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		State state2 = schema.getState("state2");
		
		// Disqual test2 and test3 when traversed by ssn4
		core.addHookListener(new TestQualificationHookListenerDisqualifyTest(true, "ssn4", schema.getTest("test2")));
		core.addHookListener(new TestQualificationHookListenerDisqualifyTest(false, "ssn4", schema.getTest("test3")));
		
		VariantCoreSession ssn4 = core.getSession("ssn4");
		setTargetingStabile(ssn4, "test2.C","test3.A","test4.A","test5.B","test6.C");
		VariantCoreStateRequest request = ssn4.targetForState(state2);
		VariantEvent customEvent = new CustomEvent("foo", "bar");
		customEvent.getParameterMap().put("param-key", "param-value");
		ssn4.triggerEvent(customEvent);
		assertNotNull(request.getStateVisitedEvent());

		ssn4.getStateRequest().commit();

		// Wait for the async writer thread to commit;
		Thread.sleep(500);
		
		// Read only events generated by this test.
		Collection<VariantEventFromDatabase> events = new EventReader(core).readEvents(
				new Predicate<VariantEventFromDatabase>() {
					@Override
					public boolean evaluate(VariantEventFromDatabase e) {
						return "ssn4".equals(e.getSessionId());
					}
				});
		assertEquals(2, events.size());

		for (VariantEventFromDatabase e: events) {
			if (e.getEventValue().equals("bar")) {
				assertEquals("foo", e.getEventName());
				assertEquals("ssn4", e.getSessionId());
				assertEquals("bar", e.getEventValue());
				assertEquals(1, e.getParameterMap().size());
				assertEquals("param-value", e.getParameterMap().get("param-key"));
				Collection<EventExperienceFromDatabase> exs = e.getEventExperiences();
				assertEquals(4, e.getEventExperiences().size());
				for (EventExperienceFromDatabase ee: e.getEventExperiences()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else if (e.getEventValue().equals("state2")) {
				assertEquals(StateVisitedEvent.EVENT_NAME, e.getEventName());
				assertEquals("ssn4", e.getSessionId());
				assertEquals("state2", e.getEventValue());
				assertEquals(2, e.getParameterMap().size());
				assertEquals("/path/to/state2/test5.B", e.getParameterMap().get("PATH"));
				assertEquals("OK", e.getParameterMap().get("REQ_STATUS"));
				assertEquals(4, e.getEventExperiences().size());
				for (EventExperienceFromDatabase ee: e.getEventExperiences()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Unexpected event value [" + e.getEventValue() + "]", false);
		}
		new JdbcService(core).recreateSchema();
		
		core.finalize();
	}

	/**
	 * Disqualify the test(s) in a session.
	 */
	private static class TestQualificationHookListenerDisqualifyTest implements HookListener<TestQualificationHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		private com.variant.core.schema.Test[] testsToDisqualify;
		private String sessionId;
		private boolean removeFromTt;
		
		private TestQualificationHookListenerDisqualifyTest(boolean removeFromTt, String sessionId, com.variant.core.schema.Test...testsToDisqualify) {
			this.testsToDisqualify = testsToDisqualify;
			this.sessionId = sessionId;
			this.removeFromTt = removeFromTt;
		}

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			if (! hook.getSession().getId().equals(sessionId)) return;
			
			boolean found = false;
			for (com.variant.core.schema.Test test: testsToDisqualify) {
				if (test.equals(hook.getTest())) {
					found = true;
					break;
				}
			}
			
			if (found) {
				testList.add(hook.getTest());
				hook.setQualified(false);
				hook.setRemoveFromTargetingTracker(removeFromTt);
			}
		}		
	}

	/**
	 * 
	 */
	private static class CustomEvent implements VariantEvent {

		private String name, value;
		private HashMap<String, Object> params = new HashMap<String, Object>();
		private Date createDate = new Date();
		
		private CustomEvent(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public String getEventName() {
			return name;
		}

		@Override
		public String getEventValue() {
			return value;
		}

		@Override
		public Map<String, Object> getParameterMap() {
			return params;
		}

		@Override
		public Date getCreateDate() {
			return createDate;
		}
		
	}

}
