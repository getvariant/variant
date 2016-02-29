package com.variant.core.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.StateParsedHook;
import com.variant.core.hook.TestParsedHook;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantCollectionsUtils;

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
public class UserHookTest extends BaseTest {

	private static final String MESSAGE_TEXT_STATE = "Info-Message-State";
	private static final String MESSAGE_TEXT_TEST = "Info-Message-Test";
	
	@Test
	public void stateParsedTest() throws Exception {
		
		api = (VariantCoreImpl) Variant.Factory.getInstance();
		
		StateParsedHookListenerImpl listener = new StateParsedHookListenerImpl();
		api.addHookListener(listener);
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(api.getSchema().getStates(), listener.stateList);
		assertEquals(api.getSchema().getStates().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_STATE, msg.getText());
			
		}
	}

	@Test
	public void testParsedTest() throws Exception {
		
		api = (VariantCoreImpl) Variant.Factory.getInstance();
		
		TestParsedHookListenerImpl listener = new TestParsedHookListenerImpl();
		api.clearHookListeners();
		api.addHookListener(listener);
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(api.getSchema().getTests(), listener.testList);
		assertEquals(api.getSchema().getTests().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_TEST, msg.getText());
			
		}

		StateParsedHookListenerImpl stateListener = new StateParsedHookListenerImpl();
		api.addHookListener(stateListener);
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(VariantCollectionsUtils.list(api.getSchema().getTests(), api.getSchema().getTests()), listener.testList);
		assertEquals(api.getSchema().getStates(), stateListener.stateList);
		assertEquals(api.getSchema().getTests().size() + api.getSchema().getStates().size(), response.getMessages().size());

		for (int i = 0; i < response.getMessages().size(); i++) {
			ParserMessage msg = response.getMessages().get(i);
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(i < api.getSchema().getStates().size() ? MESSAGE_TEXT_STATE : MESSAGE_TEXT_TEST, msg.getText());
		}

	}

	@Test
	public void testQualificationTest() throws Exception {
		
		api = (VariantCoreImpl) Variant.Factory.getInstance();
		
		TestQualificationHookListenerNullImpl nullListener = new TestQualificationHookListenerNullImpl();
		api.clearHookListeners();
		api.addHookListener(nullListener);
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		assertTrue(nullListener.testList.isEmpty());
		Schema schema = api.getSchema();
		State state1 = schema.getState("state1");
		VariantSession ssn = api.getSession("foo");
		VariantStateRequest request = api.dispatchRequest(ssn, state1, "");
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(
				ssn.getTraversedTests(), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("test1"), true), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("Test1"), true));
		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("Test1")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		api.commitStateRequest(request, "");
		
		nullListener.testList.clear();
		
		// Repeat the same thing.  Test should have been put on the qualified list for the session
		// so the hooks won't be posted.
		assertTrue(nullListener.testList.isEmpty());
		request = api.dispatchRequest(ssn, state1, "");
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(2, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(
				ssn.getTraversedTests(), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("test1"), true), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("Test1"), true));

		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("Test1")));
		assertEquals(0, nullListener.testList.size());
		api.commitStateRequest(request, "");

		// New session. Disqualify, but keep in TT.
		TestQualificationHookListenerDisqualifyImpl disqualListener = new TestQualificationHookListenerDisqualifyImpl(false, schema.getTest("test1"));
		api.addHookListener(disqualListener);
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		assertTrue(disqualListener.testList.isEmpty());
		schema = api.getSchema();
		state1 = schema.getState("state1");
		ssn = api.getSession("foo2");
		request = api.dispatchRequest(ssn, state1, targetingTrackerString("test2.D","Test1.A"));
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(
				ssn.getTraversedTests(), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("test1"), false), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("Test1"), true));

		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNull(request.getTargetingTracker().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("Test1")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1")), disqualListener.testList);
		assertEquals("/path/to/state1/Test1.A", request.getResolvedParameterMap().get("path"));
		api.commitStateRequest(request, "");

		// New session. Disqualify and drop from TT
		disqualListener = new TestQualificationHookListenerDisqualifyImpl(true, schema.getTest("Test1"));
		api.clearHookListeners();
		api.addHookListener(disqualListener);
		
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(disqualListener.testList.isEmpty());
		schema = api.getSchema();
		state1 = schema.getState("state1");
		ssn = api.getSession("foo3");
		request = api.dispatchRequest(ssn, state1, targetingTrackerString("test1.B","test2.D","Test1.A"));
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(
				ssn.getTraversedTests(), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("test1"), true), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("Test1"), false));

		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNull(request.getTargetingTracker().get(schema.getTest("Test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test2")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("Test1")), disqualListener.testList);
		assertEquals("/path/to/state1/test1.B", request.getResolvedParameterMap().get("path"));
		api.commitStateRequest(request, "");

		// Same session, but dispatch o state2 - it's only instrumented by the off test2. 
		// The extra disqualifier should not matter because test1 has already been qualified for this session. The
		disqualListener = new TestQualificationHookListenerDisqualifyImpl(true, schema.getTest("Test1"), schema.getTest("test1"));
		api.addHookListener(disqualListener);
		
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(disqualListener.testList.isEmpty());
		schema = api.getSchema();
		state1 = schema.getState("state1");
		State state2 = schema.getState("state2");
		ssn = api.getSession("foo3");
		request = api.dispatchRequest(ssn, state2, targetingTrackerString("test1.B","test2.D","Test1.A"));
		assertEquals(2, ssn.getTraversedStates().size());		
		assertEqualAsSets(
				ssn.getTraversedStates(), 
				new Pair<State, Integer>(state1, 1), 
				new Pair<State, Integer>(state2, 1));

		assertEqualAsSets(
				ssn.getTraversedTests(), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("test1"), true), 
				new Pair<com.variant.core.schema.Test, Boolean>(schema.getTest("Test1"), false));

		assertEquals(3, request.getTargetingTracker().getAll().size());
		assertEquals(experience("Test1.A"), request.getTargetingTracker().get(schema.getTest("Test1")));
		assertEquals(experience("test1.B"), request.getTargetingTracker().get(schema.getTest("test1")));
		assertEquals(experience("test2.D"), request.getTargetingTracker().get(schema.getTest("test2")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test2")));
		assertTrue(disqualListener.testList.isEmpty());
		assertEquals("/path/to/state2", request.getResolvedParameterMap().get("path"));
		api.commitStateRequest(request, "");

	}

	/**
	 * 
	 */
	private static class StateParsedHookListenerImpl implements HookListener<StateParsedHook> {

		private ArrayList<State> stateList = new ArrayList<State>();

		@Override
		public Class<StateParsedHook> getHookClass() {
			return StateParsedHook.class;
		}
		
		@Override
		public void post(StateParsedHook hook) {
			stateList.add(hook.getState());
			hook.getParserResponse().addMessage(Severity.INFO, MESSAGE_TEXT_STATE);
		}
		
	}
	
	/**
	 * 
	 */
	private static class TestParsedHookListenerImpl implements HookListener<TestParsedHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		
		@Override
		public Class<TestParsedHook> getHookClass() {
			return TestParsedHook.class;
		}

		@Override
		public void post(TestParsedHook hook) {
			testList.add(hook.getTest());
			hook.getParserResponse().addMessage(Severity.INFO, MESSAGE_TEXT_TEST);
		}		
	}

	/**
	 * Do nothing.  Tests should be qualified.
	 */
	private static class TestQualificationHookListenerNullImpl implements HookListener<TestQualificationHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		
		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {

			testList.add(hook.getTest());
		}		
	}

	/**
	 * 
	 */
	private static class TestQualificationHookListenerDisqualifyImpl implements HookListener<TestQualificationHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		private com.variant.core.schema.Test[] testsToDisqualify;
		private boolean removeFromTt;
		
		private TestQualificationHookListenerDisqualifyImpl(boolean removeFromTt, com.variant.core.schema.Test...testsToDisqualify) {
			this.testsToDisqualify = testsToDisqualify;
			this.removeFromTt = removeFromTt;
		}

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			
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

}
