package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.flashpoint.StateParsedFlashpoint;
import com.variant.core.flashpoint.TestParsedFlashpoint;
import com.variant.core.flashpoint.TestQualificationFlashpoint;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantStringUtils;

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
public class FlashpointTest extends BaseTest {

	private static final String MESSAGE_TEXT_STATE = "Info-Message-State";
	private static final String MESSAGE_TEXT_TEST = "Info-Message-Test";
	
	@Test
	public void stateParsedTest() throws Exception {
		
		Variant variant = Variant.Factory.getInstance();
		
		StateParsedFlashpointListenerImpl listener = new StateParsedFlashpointListenerImpl();
		variant.addFlashpointListener(listener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(variant.getSchema().getStates(), listener.stateList);
		assertEquals(variant.getSchema().getStates().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_STATE, msg.getMessage());
			
		}
	}

	@Test
	public void testParsedTest() throws Exception {
		
		Variant variant = Variant.Factory.getInstance();
		
		TestParsedFlashpointListenerImpl listener = new TestParsedFlashpointListenerImpl();
		variant.clearFlashpointListeners();
		variant.addFlashpointListener(listener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(variant.getSchema().getTests(), listener.testList);
		assertEquals(variant.getSchema().getTests().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_TEST, msg.getMessage());
			
		}

		StateParsedFlashpointListenerImpl stateListener = new StateParsedFlashpointListenerImpl();
		variant.addFlashpointListener(stateListener);
		response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(VariantCollectionsUtils.list(variant.getSchema().getTests(), variant.getSchema().getTests()), listener.testList);
		assertEquals(variant.getSchema().getStates(), stateListener.stateList);
		assertEquals(variant.getSchema().getTests().size() + variant.getSchema().getStates().size(), response.getMessages().size());

		for (int i = 0; i < response.getMessages().size(); i++) {
			ParserMessage msg = response.getMessages().get(i);
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(i < variant.getSchema().getStates().size() ? MESSAGE_TEXT_STATE : MESSAGE_TEXT_TEST, msg.getMessage());
		}

	}

	@Test
	public void testQualificationTest() throws Exception {
		
		Variant variant = Variant.Factory.getInstance();
		
		TestQualificationFlashpointListenerNullImpl nullListener = new TestQualificationFlashpointListenerNullImpl();
		variant.clearFlashpointListeners();
		variant.addFlashpointListener(nullListener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		Schema schema = variant.getSchema();
		State state = schema.getState("state1");
		VariantSession ssn = engine.getSession("foo");
		//String tpData = System.currentTimeMillis() + ".state1.A";
		VariantStateRequest request = engine.newStateRequest(ssn, state, "");
		assertTrue(request.getDisqualifiedTests().isEmpty());
		assertEquals(2, request.getTargetingPersister().getAll().size());
		assertNotNull(request.getTargetingPersister().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingPersister().get(schema.getTest("Test1")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		
		nullListener.testList.clear();
		
		TestQualificationFlashpointListenerDisqualifyImpl disqualListener = new TestQualificationFlashpointListenerDisqualifyImpl(schema.getTest("test1"));
		variant.addFlashpointListener(disqualListener);
		response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		assertTrue(disqualListener.testList.isEmpty());
		schema = variant.getSchema();
		state = schema.getState("state1");
		ssn = engine.getSession("foo");
		long timestamp = System.currentTimeMillis();
		String tpString = timestamp + ".test2.D|" + timestamp + ".Test1.A"; 
		request = engine.newStateRequest(ssn, state, tpString);
		assertEquals(1, request.getDisqualifiedTests().size());
		assertEquals(2, request.getTargetingPersister().getAll().size());
		assertNotNull(request.getTargetingPersister().get(schema.getTest("test2")));
		assertNotNull(request.getTargetingPersister().get(schema.getTest("Test1")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1"), schema.getTest("Test1")), nullListener.testList);
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1")), disqualListener.testList);
		assertEquals("/path/to/state1/Test1.A", request.getResolvedParameterMap().get("path"));

		disqualListener = new TestQualificationFlashpointListenerDisqualifyImpl(schema.getTest("Test1"), true);
		variant.clearFlashpointListeners();
		variant.addFlashpointListener(disqualListener);
		
		response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(disqualListener.testList.isEmpty());
		schema = variant.getSchema();
		state = schema.getState("state1");
		ssn = engine.getSession("foo");
		tpString = timestamp + ".test1.B|" + timestamp + ".test2.D|" + timestamp + ".Test1.A"; 
		request = engine.newStateRequest(ssn, state, tpString);
		assertEquals(1, request.getDisqualifiedTests().size());
		assertEquals(2, request.getTargetingPersister().getAll().size());
		assertNotNull(request.getTargetingPersister().get(schema.getTest("test1")));
		assertNotNull(request.getTargetingPersister().get(schema.getTest("test2")));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("Test1")), disqualListener.testList);
		assertEquals("/path/to/state1/test1.B", request.getResolvedParameterMap().get("path"));

	}

	/**
	 * 
	 */
	private static class StateParsedFlashpointListenerImpl implements FlashpointListener<StateParsedFlashpoint> {

		private ArrayList<State> stateList = new ArrayList<State>();

		@Override
		public Class<StateParsedFlashpoint> getFlashpointClass() {
			return StateParsedFlashpoint.class;
		}
		
		@Override
		public void post(StateParsedFlashpoint flashpoint) {
			stateList.add(flashpoint.getState());
			flashpoint.getParserResponse().addMessage(Severity.INFO, MESSAGE_TEXT_STATE);
		}
		
	}
	
	/**
	 * 
	 */
	private static class TestParsedFlashpointListenerImpl implements FlashpointListener<TestParsedFlashpoint> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		
		@Override
		public Class<TestParsedFlashpoint> getFlashpointClass() {
			return TestParsedFlashpoint.class;
		}

		@Override
		public void post(TestParsedFlashpoint flashpoint) {
			testList.add(flashpoint.getTest());
			flashpoint.getParserResponse().addMessage(Severity.INFO, MESSAGE_TEXT_TEST);
		}		
	}

	/**
	 * 
	 */
	private static class TestQualificationFlashpointListenerNullImpl implements FlashpointListener<TestQualificationFlashpoint> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		
		@Override
		public Class<TestQualificationFlashpoint> getFlashpointClass() {
			return TestQualificationFlashpoint.class;
		}

		@Override
		public void post(TestQualificationFlashpoint flashpoint) {

			testList.add(flashpoint.getTest());
		}		
	}

	/**
	 * 
	 */
	private static class TestQualificationFlashpointListenerDisqualifyImpl implements FlashpointListener<TestQualificationFlashpoint> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		private com.variant.core.schema.Test testToDisqualify;
		private Boolean removeFromTp = null;
		
		private TestQualificationFlashpointListenerDisqualifyImpl(com.variant.core.schema.Test testToDisqualify) {
			this.testToDisqualify = testToDisqualify;
		}

		private TestQualificationFlashpointListenerDisqualifyImpl(com.variant.core.schema.Test testToDisqualify, boolean removeFromTp) {
			this.testToDisqualify = testToDisqualify;
			this.removeFromTp = removeFromTp;
		}

		@Override
		public Class<TestQualificationFlashpoint> getFlashpointClass() {
			return TestQualificationFlashpoint.class;
		}

		@Override
		public void post(TestQualificationFlashpoint flashpoint) {
			if (flashpoint.getTest().equals(testToDisqualify)) {
				testList.add(flashpoint.getTest());
				flashpoint.setQualified(false);
				if (removeFromTp != null) flashpoint.setRemoveFromTargetingPersister(removeFromTp);
			}
		}		
	}

}
