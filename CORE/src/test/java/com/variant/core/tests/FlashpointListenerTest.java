package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.flashpoint.StateParsedFlashpoint;
import com.variant.core.flashpoint.StateParsedFlashpointListener;
import com.variant.core.flashpoint.TestParsedFlashpoint;
import com.variant.core.flashpoint.TestParsedFlashpointListener;
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
public class FlashpointListenerTest extends BaseTest {

	private static final String MESSAGE_TEXT_STATE = "Info-Message-State";
	private static final String MESSAGE_TEXT_TEST = "Info-Message-Test";
	
	@Test
	public void viewParsedListenerTest() throws Exception {
		
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
	public void testParsedListenerTest() throws Exception {
		
		Variant variant = Variant.Factory.getInstance();
		
		TestParsedFlashpointListenerImpl testListener = new TestParsedFlashpointListenerImpl();
		variant.clearFlashpointListeners();
		variant.addFlashpointListener(testListener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(variant.getSchema().getTests(), testListener.testList);
		assertEquals(variant.getSchema().getTests().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_TEST, msg.getMessage());
			
		}

		StateParsedFlashpointListenerImpl stateListener = new StateParsedFlashpointListenerImpl();
		variant.addFlashpointListener(stateListener);
		response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		assertEquals(VariantCollectionsUtils.list(variant.getSchema().getTests(), variant.getSchema().getTests()), testListener.testList);
		assertEquals(variant.getSchema().getStates(), stateListener.stateList);
		assertEquals(variant.getSchema().getTests().size() + variant.getSchema().getStates().size(), response.getMessages().size());

		for (int i = 0; i < response.getMessages().size(); i++) {
			ParserMessage msg = response.getMessages().get(i);
			assertEquals(Severity.INFO, msg.getSeverity());
			System.out.println(msg.getMessage());
			assertEquals(i < variant.getSchema().getStates().size() ? MESSAGE_TEXT_STATE : MESSAGE_TEXT_TEST, msg.getMessage());
		}

	}

	/**
	 * 
	 */
	private static class StateParsedFlashpointListenerImpl implements StateParsedFlashpointListener {

		private ArrayList<State> stateList = new ArrayList<State>();

		@Override
		public void reached(StateParsedFlashpoint flashpoint) {
			stateList.add(flashpoint.getState());
			flashpoint.getParserResponse().addMessage(Severity.INFO, MESSAGE_TEXT_STATE);
		}
		
	}
	
	/**
	 * 
	 */
	private static class TestParsedFlashpointListenerImpl implements TestParsedFlashpointListener {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		
		@Override
		public void reached(TestParsedFlashpoint flashpoint) {
			testList.add(flashpoint.getTest());
			flashpoint.getParserResponse().addMessage(Severity.INFO, MESSAGE_TEXT_TEST);
		}		
	}

}
