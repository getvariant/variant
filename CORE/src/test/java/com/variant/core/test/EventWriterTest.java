package com.variant.core.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.Predicate;
import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.StateVisitedEvent;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.jdb.test.EventReader;
import com.variant.core.jdb.test.VariantEventFromDatabase;
import com.variant.core.jdb.test.VariantEventVariantFromDatabase;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

public class EventWriterTest extends BaseTest {
		

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void stateVisitedEventTest() throws Exception {

		ParserResponse response = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = api.getSchema();
		State s1 = schema.getState("state1");
		State s2 = schema.getState("state2");
		
		VariantSession ssn1 = api.getSession("ssn1");
		VariantStateRequest request = api.dispatchRequest(ssn1, s1, targetingTrackerString("test1.A","test2.B","test3.C","test4.A","test5.B","test6.C"));
		assertNotNull(request.getStateVisitedEvent());

		VariantSession ssn2 = api.getSession("ssn2");
		request = api.dispatchRequest(ssn2, s2, targetingTrackerString("test2.C","test3.A","test4.A","test5.B","test6.C"));
		assertNotNull(request.getStateVisitedEvent());

		api.commitStateRequest(ssn1.getStateRequest(), "");
		api.commitStateRequest(ssn2.getStateRequest(), "");

		// Wait for the async writer thread to commit;
		Thread.sleep(500);
		
		// Read only events generated by this test.
		Collection<VariantEventFromDatabase> events = EventReader.readEvents(
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
				assertEquals("/path/to/state1/test5.B+test6.C", e.getParameterMap().get("PATH"));
				assertEquals("OK", e.getParameterMap().get("REQ_STATUS"));
				assertEquals(5, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test2")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
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
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Unexpected event value [" + e.getEventValue() + "]", false);
		}

		JdbcUtil.recreateSchema();

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void customEventTest1() throws Exception {

		ParserResponse response = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = api.getSchema();
		State s2 = schema.getState("state2");
		
		VariantSession ssn3 = api.getSession("ssn3");
		VariantStateRequest request = api.dispatchRequest(ssn3, s2, targetingTrackerString("test2.C","test3.A","test4.A","test5.B","test6.C"));
		VariantEvent customEvent = new CustomEvent("foo", "bar");
		customEvent.getParameterMap().put("param-key", "param-value");
		ssn3.triggerEvent(customEvent);
		assertNotNull(request.getStateVisitedEvent());

		api.commitStateRequest(ssn3.getStateRequest(), "");

		// Wait for the async writer thread to commit;
		Thread.sleep(500);
		
		// Read only events generated by this test.
		Collection<VariantEventFromDatabase> events = EventReader.readEvents(
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
				assertEquals(6, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
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
				assertEquals(6, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test2")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test3")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Unexpected event value [" + e.getEventValue() + "]", false);
		}
		JdbcUtil.recreateSchema();
	}
	
	/**
	 * Same thing but test2 and test3 are disqualified.
	 * @throws Exception
	 */
	@Test
	public void customEventTest2() throws Exception {

		ParserResponse response = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = api.getSchema();
		State s2 = schema.getState("state2");
		
		api.addHookListener(new TestQualificationHookListenerDisqualifyTest(true, "ssn4", schema.getTest("test2")));
		api.addHookListener(new TestQualificationHookListenerDisqualifyTest(false, "ssn4", schema.getTest("test3")));
		
		VariantSession ssn4 = api.getSession("ssn4");
		VariantStateRequest request = api.dispatchRequest(ssn4, s2, targetingTrackerString("test2.C","test3.A","test4.A","test5.B","test6.C"));
		VariantEvent customEvent = new CustomEvent("foo", "bar");
		customEvent.getParameterMap().put("param-key", "param-value");
		ssn4.triggerEvent(customEvent);
		assertNotNull(request.getStateVisitedEvent());

		api.commitStateRequest(ssn4.getStateRequest(), "");

		// Wait for the async writer thread to commit;
		Thread.sleep(500);
		
		// Read only events generated by this test.
		Collection<VariantEventFromDatabase> events = EventReader.readEvents(
				new Predicate<VariantEventFromDatabase>() {
					@Override
					public boolean evaluate(VariantEventFromDatabase e) {
						return "ssn4".equals(e.getSessionId());
					}
				});
		assertEquals(2, events.size());
		assertEquals(2, events.size());

		for (VariantEventFromDatabase e: events) {
			if (e.getEventValue().equals("bar")) {
				assertEquals("foo", e.getEventName());
				assertEquals("ssn4", e.getSessionId());
				assertEquals("bar", e.getEventValue());
				assertEquals(1, e.getParameterMap().size());
				assertEquals("param-value", e.getParameterMap().get("param-key"));
				assertEquals(4, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
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
				assertEquals(4, e.getEventVariants().size());
				for (VariantEventVariantFromDatabase ee: e.getEventVariants()) {
					if (ee.getTestName().equals("test1")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test4")) {
						assertEquals("A", ee.getExperienceName());
						assertTrue(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test5")) {
						assertEquals("B", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else if (ee.getTestName().equals("test6")) {
						assertEquals("C", ee.getExperienceName());
						assertFalse(ee.isExperienceControl());
					}
					else assertTrue("Not supposed to be here", false);
				}
			}
			else assertTrue("Unexpected event value [" + e.getEventValue() + "]", false);
		}
		JdbcUtil.recreateSchema();
	}

	/**
	 * 
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