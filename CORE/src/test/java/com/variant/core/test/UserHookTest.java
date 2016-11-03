package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.Error;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.Tuples.Pair;
import com.variant.server.ParserMessage;
import com.variant.server.ParserResponse;
import com.variant.server.ParserMessage.Severity;
import com.variant.server.hook.HookListener;
import com.variant.server.hook.StateParsedHook;
import com.variant.server.hook.TestParsedHook;
import com.variant.server.hook.TestQualificationHook;
import com.variant.server.hook.TestTargetingHook;

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
public class UserHookTest extends BaseTestCore {

	private static final String MESSAGE_TEXT_STATE = "Info-Message-State";
	private static final String MESSAGE_TEXT_TEST = "Info-Message-Test";
	
	private Random rand = new Random();
	
	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void stateParsedTest() throws Exception {
			
		VariantCore core = rebootApi();
		StateParsedHookListenerImpl listener = new StateParsedHookListenerImpl();
		core.addHookListener(listener);
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		assertEquals(core.getSchema().getStates(), listener.stateList);
		assertEquals(core.getSchema().getStates().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_STATE, msg.getText());
			
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void testParsedTest() throws Exception {
		
		VariantCore core = rebootApi();
		TestParsedHookListenerImpl listener = new TestParsedHookListenerImpl();
		core.clearHookListeners();
		core.addHookListener(listener);
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		assertEquals(core.getSchema().getTests(), listener.testList);
		assertEquals(core.getSchema().getTests().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_TEST, msg.getText());
			
		}

		StateParsedHookListenerImpl stateListener = new StateParsedHookListenerImpl();
		core.addHookListener(stateListener);
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		assertEquals(VariantCollectionsUtils.list(core.getSchema().getTests(), core.getSchema().getTests()), listener.testList);
		assertEquals(core.getSchema().getStates(), stateListener.stateList);
		assertEquals(core.getSchema().getTests().size() + core.getSchema().getStates().size(), response.getMessages().size());

		for (int i = 0; i < response.getMessages().size(); i++) {
			ParserMessage msg = response.getMessages().get(i);
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(i < core.getSchema().getStates().size() ? MESSAGE_TEXT_STATE : MESSAGE_TEXT_TEST, msg.getText());
		}

	}

	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void testQualificationTest() throws Exception {
		
		VariantCore core = rebootApi();
		TestQualificationHookListenerNullImpl nullListener = new TestQualificationHookListenerNullImpl();
		core.clearHookListeners();
		core.addHookListener(nullListener);
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		assertTrue(nullListener.testList.isEmpty());
		Schema schema = core.getSchema();
		State state1 = schema.getState("state1");
		State state2 = schema.getState("state2");
		String sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn = core.getSession(sessionId, true).getBody();

		VariantCoreStateRequest request = ssn.targetForState(state1);
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(ssn.getTraversedTests(), schema.getTest("test1"), schema.getTest("Test1"));
		assertEquals(0, ssn.getDisqualifiedTests().size());
		SessionScopedTargetingStabile stabile = ((CoreSession)ssn).getTargetingStabile();
		assertEquals(2, stabile.getAll().size());
		assertNotNull(stabile.get("test1"));
		assertNull(stabile.get("test2"));
		assertNotNull(stabile.get("Test1"));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		request.commit();
		
		// Targeting for state2 shouldn't change anything
		request = ssn.targetForState(state2);
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(ssn.getTraversedTests(), schema.getTest("test1"), schema.getTest("Test1"));
		assertEquals(0, ssn.getDisqualifiedTests().size());
		stabile = ((CoreSession)ssn).getTargetingStabile();
		assertEquals(2, stabile.getAll().size());
		assertNotNull(stabile.get("test1"));
		assertNull(stabile.get("test2"));
		assertNotNull(stabile.get("Test1"));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		request.commit();
		
		nullListener.testList.clear();
		
		// Repeat the same thing.  Test should have been put on the qualified list for the session
		// so the hooks won't be posted.
		assertTrue(nullListener.testList.isEmpty());
		request = ssn.targetForState(state1);
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(2, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(ssn.getTraversedTests(), schema.getTest("test1"), schema.getTest("Test1"));

		assertEquals(2, stabile.getAll().size());
		assertNotNull(stabile.get("test1"));
		assertNotNull(stabile.get("Test1"));
		assertEquals(0, nullListener.testList.size());
		request.commit();

		// New session. Disqualify, but keep in TT.
		TestQualificationHookListenerDisqualifyImpl disqualListener = new TestQualificationHookListenerDisqualifyImpl(false, schema.getTest("test1"));
		core.addHookListener(disqualListener);
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		assertTrue(disqualListener.testList.isEmpty());
		schema = core.getSchema();
		state1 = schema.getState("state1");
		sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn2 = core.getSession(sessionId, true).getBody();
		setTargetingStabile(ssn2, "test2.D", "Test1.A");
		request = ssn2.targetForState(state1);
		assertEquals(1, ssn2.getTraversedStates().size());
		assertEquals(state1, ssn2.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn2.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(ssn2.getTraversedTests(), schema.getTest("Test1"));
		assertEqualAsSets(ssn2.getDisqualifiedTests(), schema.getTest("test1"));

		assertEquals(2, stabile.getAll().size());
		assertNotNull(stabile.get("test1"));
		assertNotNull(stabile.get("Test1"));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1")), disqualListener.testList);
		assertEquals("/path/to/state1/Test1.A", request.getResolvedParameter("path"));
		request.commit();

		// New session. Disqualify and drop from TT
		core.clearHookListeners();
		disqualListener = new TestQualificationHookListenerDisqualifyImpl(true, schema.getTest("Test1"));
		core.addHookListener(disqualListener);
		
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(disqualListener.testList.isEmpty());
		schema = core.getSchema();
		state1 = schema.getState("state1");
		sessionId = VariantStringUtils.random64BitString(rand);
		CoreSession ssn3 = (CoreSession) core.getSession(sessionId, true).getBody();
		assertTrue(ssn3.getTraversedStates().isEmpty());
		assertTrue(ssn3.getTraversedTests().isEmpty());
		setTargetingStabile(ssn3, "test1.B","test2.D","Test1.A");
		request = ssn3.targetForState(state1);
		assertEqualAsSets(ssn3.getTraversedStates(), new Pair<State, Integer>(state1, 1));
		assertEqualAsSets(ssn3.getTraversedTests(), schema.getTest("test1"));
		assertEqualAsSets(ssn3.getDisqualifiedTests(), schema.getTest("Test1"));

		VariantCoreSession ssn3uncommitted = core.getSession(sessionId, true).getBody();
		assertTrue(ssn3uncommitted.getTraversedStates().isEmpty());
		assertTrue(ssn3uncommitted.getTraversedTests().isEmpty());

		stabile = ssn3.getTargetingStabile();
		assertEquals(2, stabile.getAll().size());
		assertNull(stabile.get("Test1"));
		assertNotNull(stabile.get("test1"));
		assertNotNull(stabile.get("test2"));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("Test1")), disqualListener.testList);
		assertEquals("/path/to/state1/test1.B", request.getResolvedParameter("path"));
		request.commit();

		// Same session, but dispatch to state2 - it's only instrumented by the off test2. 
		// The extra disqualifier should not matter because test1 has already been qualified for this session. The
		disqualListener = new TestQualificationHookListenerDisqualifyImpl(true, schema.getTest("Test1"), schema.getTest("test1"));
		core.addHookListener(disqualListener);
		
		assertTrue(disqualListener.testList.isEmpty());
		schema = core.getSchema();
		state1 = schema.getState("state1");
		state2 = schema.getState("state2");
		setTargetingStabile(ssn3, "test1.B","test2.D","Test1.A");
		request = ssn3.targetForState(state2);
		assertEqualAsSets(ssn3.getTraversedStates(), new Pair<State, Integer>(state1, 1));

		assertEqualAsSets(ssn3.getTraversedTests(), schema.getTest("test1"));

		stabile = ssn3.getTargetingStabile();
		assertEquals(3, stabile.getAll().size());
		assertEquals("A", stabile.get("Test1").getExperienceName());
		assertEquals("B", stabile.get("test1").getExperienceName());
		assertEquals("D", stabile.get("test2").getExperienceName());
		assertTrue(disqualListener.testList.isEmpty());
		assertEquals("/path/to/state2", request.getResolvedParameter("path"));
		request.commit();
		assertEqualAsSets(ssn3.getTraversedStates(), new Pair<State, Integer>(state1, 1));

		assertEqualAsSets(ssn3.getTraversedTests(), schema.getTest("test1"));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void testTargetingTest() throws Exception {
		
		final VariantCore core = rebootApi();
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		final Test t1 = schema.getTest("test1");
		final Test t2 = schema.getTest("test2");
		final Test t3 = schema.getTest("Test1");
		final State s1 = schema.getState("state1");
		final State s2 = schema.getState("state2");
		final State s3 = schema.getState("state3");

		core.clearHookListeners();
		
		// Listen to targeting posts from the off test "test2". Should never be posted.
		TargetingHookListener test2Listener = new TargetingHookListener(t2, t2.getControlExperience());
		core.addHookListener(test2Listener);
				
		String sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
		ssn.targetForState(s1).commit();
		assertEquals("Off tests should not be targeted", 0, test2Listener.count);
		ssn.targetForState(s2).commit();
		assertEquals("Off tests should not be targeted", 0, test2Listener.count);
		ssn.targetForState(s3).commit();
		assertEquals("Off tests should not be targeted", 0, test2Listener.count);
		
		// Listen to targeting posts from the on test "test1". Should be posted.
		TargetingHookListener test1Listener = new TargetingHookListener(t1, t1.getControlExperience());
		core.addHookListener(test1Listener);

		sessionId = VariantStringUtils.random64BitString(rand);
		ssn = core.getSession(sessionId, true).getBody();
		ssn.targetForState(s1).commit();
		assertEquals(1, test1Listener.count);
		ssn.targetForState(s2).commit();
		assertEquals(1, test1Listener.count);
		ssn.targetForState(s3).commit();
		assertEquals(1, test1Listener.count);

		// Return the wrong experience. Should throw runtime user exception.
		final TargetingHookListener test1BadListener = new TargetingHookListener(t1, t3.getControlExperience());
		core.addHookListener(test1BadListener);
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() {		
				String sessionId = VariantStringUtils.random64BitString(rand);
				VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
				ssn.targetForState(s2).commit();  // Not instrumented on s2
				assertEquals("Off tests should not be targeted", 0, test1BadListener.count);
				ssn.targetForState(s1);
			}
		}.assertThrown(Error.RUN_HOOK_TARGETING_BAD_EXPERIENCE, test1BadListener.getClass().getName(), t1.getName(), t3.getControlExperience().toString());

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
			hook.addMessage(Severity.INFO, MESSAGE_TEXT_STATE);
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
			hook.addMessage(Severity.INFO, MESSAGE_TEXT_TEST);
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
		
		private TestQualificationHookListenerDisqualifyImpl(boolean removeFromTt, Test...testsToDisqualify) {
			this.testsToDisqualify = testsToDisqualify;
			this.removeFromTt = removeFromTt;
		}

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			assertNotNull(hook.getSession());
			assertNotNull(hook.getTest());
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
	private static class TargetingHookListener implements HookListener<TestTargetingHook> {

		private int count = 0;
		private Test forTest;
		private Experience targetExperience;
		
		/**
		 * Target for experience exp if posted for test test.
		 * @param test
		 * @param exp 
		 */
		TargetingHookListener(Test forTest, Experience targetExperience) {
			this.forTest = forTest;
			this.targetExperience = targetExperience;
		}
		
		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}

		@Override
		public void post(TestTargetingHook hook) {
			assertNotNull(hook.getSession());
			assertNotNull(hook.getTest());
			assertNotNull(hook.getState());
			if (hook.getTest().equals(forTest)) {
				count++;
				hook.setTargetedExperience(targetExperience);
			}
		}
		
	};

}
