package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.StateParsedHook;
import com.variant.core.hook.TestParsedHook;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
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
		
		Variant variant = Variant.Factory.getInstance();
		
		StateParsedHookListenerImpl listener = new StateParsedHookListenerImpl();
		variant.addHookListener(listener);
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(variant.getSchema().getStates(), listener.stateList);
		assertEquals(variant.getSchema().getStates().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_STATE, msg.getText());
			
		}
	}

	@Test
	public void testParsedTest() throws Exception {
		
		Variant variant = Variant.Factory.getInstance();
		
		TestParsedHookListenerImpl listener = new TestParsedHookListenerImpl();
		variant.clearHookListeners();
		variant.addHookListener(listener);
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(variant.getSchema().getTests(), listener.testList);
		assertEquals(variant.getSchema().getTests().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_TEST, msg.getText());
			
		}

		StateParsedHookListenerImpl stateListener = new StateParsedHookListenerImpl();
		variant.addHookListener(stateListener);
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(VariantCollectionsUtils.list(variant.getSchema().getTests(), variant.getSchema().getTests()), listener.testList);
		assertEquals(variant.getSchema().getStates(), stateListener.stateList);
		assertEquals(variant.getSchema().getTests().size() + variant.getSchema().getStates().size(), response.getMessages().size());

		for (int i = 0; i < response.getMessages().size(); i++) {
			ParserMessage msg = response.getMessages().get(i);
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(i < variant.getSchema().getStates().size() ? MESSAGE_TEXT_STATE : MESSAGE_TEXT_TEST, msg.getText());
		}

	}

	@Test
	public void testQualificationTest() throws Exception {
		
		Variant variant = Variant.Factory.getInstance();
		
		TestQualificationHookListenerNullImpl nullListener = new TestQualificationHookListenerNullImpl();
		variant.clearHookListeners();
		variant.addHookListener(nullListener);
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		Schema schema = variant.getSchema();
		State state = schema.getState("state1");
		VariantSession ssn = api.getSession("foo");
		//String tpData = System.currentTimeMillis() + ".state1.A";
		VariantStateRequest request = api.dispatchRequest(ssn, state, "");
		assertTrue(request.getDisqualifiedTests().isEmpty());
		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("Test1")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		api.commitStateRequest(request, "");
		
		nullListener.testList.clear();
		
		TestQualificationHookListenerDisqualifyImpl disqualListener = new TestQualificationHookListenerDisqualifyImpl(schema.getTest("test1"));
		variant.addHookListener(disqualListener);
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		assertTrue(disqualListener.testList.isEmpty());
		schema = variant.getSchema();
		state = schema.getState("state1");
		ssn = api.getSession("foo");
		long timestamp = System.currentTimeMillis();
		String tpString = timestamp + ".test2.D|" + timestamp + ".Test1.A"; 
		request = api.dispatchRequest(ssn, state, tpString);
		assertEquals(1, request.getDisqualifiedTests().size());
		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test2")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("Test1")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1")), disqualListener.testList);
		assertEquals("/path/to/state1/Test1.A", request.getResolvedParameterMap().get("path"));
		api.commitStateRequest(request, "");

		disqualListener = new TestQualificationHookListenerDisqualifyImpl(schema.getTest("Test1"), true);
		variant.clearHookListeners();
		variant.addHookListener(disqualListener);
		
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(disqualListener.testList.isEmpty());
		schema = variant.getSchema();
		state = schema.getState("state1");
		ssn = api.getSession("foo");
		tpString = timestamp + ".test1.B|" + timestamp + ".test2.D|" + timestamp + ".Test1.A"; 
		request = api.dispatchRequest(ssn, state, tpString);
		assertEquals(1, request.getDisqualifiedTests().size());
		assertEquals(2, request.getTargetingTracker().getAll().size());
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingTracker().get(schema.getTest("test2")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("Test1")), disqualListener.testList);
		assertEquals("/path/to/state1/test1.B", request.getResolvedParameterMap().get("path"));
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
	 * 
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
		private com.variant.core.schema.Test testToDisqualify;
		private Boolean removeFromTp = null;
		
		private TestQualificationHookListenerDisqualifyImpl(com.variant.core.schema.Test testToDisqualify) {
			this.testToDisqualify = testToDisqualify;
		}

		private TestQualificationHookListenerDisqualifyImpl(com.variant.core.schema.Test testToDisqualify, boolean removeFromTp) {
			this.testToDisqualify = testToDisqualify;
			this.removeFromTp = removeFromTp;
		}

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			if (hook.getTest().equals(testToDisqualify)) {
				testList.add(hook.getTest());
				hook.setQualified(false);
				if (removeFromTp != null) hook.setRemoveFromTargetingTracker(removeFromTp);
			}
		}		
	}

}
