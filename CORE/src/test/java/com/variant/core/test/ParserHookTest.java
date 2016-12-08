package com.variant.core.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.exception.Error.Severity;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedHook;
import com.variant.core.schema.TestParsedHook;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.SchemaParser;

/**
 * TODO: Test parse time user hooks.
 * @author Igor
 * 
 */
public class ParserHookTest extends BaseTestCore {

	private static final String MESSAGE_TEXT_STATE = "Info-Message-State";
	private static final String MESSAGE_TEXT_TEST = "Info-Message-Test";
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void stateParsedTest() throws Exception {
			
		StateParsedHookListenerImpl listener = new StateParsedHookListenerImpl();
		UserHooker hooker = new UserHooker();
		hooker.addListener(listener);
		SchemaParser parser = new SchemaParser(hooker);
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(ParserSerialOkayTest.SCHEMA);
		Schema schema = response.getSchema();
		assertEquals(schema.getStates(), listener.stateList);
		assertEquals(schema.getStates().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_STATE, msg.getText());			
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParsedTest() throws Exception {
		
		TestParsedHookListenerImpl testListener = new TestParsedHookListenerImpl();
		UserHooker hooker = new UserHooker();
		hooker.addListener(testListener);
		SchemaParser parser = new SchemaParser(hooker);
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(ParserSerialOkayTest.SCHEMA);
		Schema schema = response.getSchema();
		assertEquals(schema.getTests(), testListener.testList);
		assertEquals(schema.getTests().size(), response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(MESSAGE_TEXT_TEST, msg.getText());
			
		}

		StateParsedHookListenerImpl stateListener = new StateParsedHookListenerImpl();
		hooker.addListener(stateListener);
		response = (ParserResponseImpl) parser.parse(ParserSerialOkayTest.SCHEMA);
		schema = response.getSchema();
		assertEquals(VariantCollectionsUtils.list(schema.getTests(), schema.getTests()), testListener.testList);
		assertEquals(schema.getStates(), stateListener.stateList);
		assertEquals(schema.getTests().size() + schema.getStates().size(), response.getMessages().size());

		for (int i = 0; i < response.getMessages().size(); i++) {
			ParserMessage msg = response.getMessages().get(i);
			assertEquals(Severity.INFO, msg.getSeverity());
			assertEquals(i < schema.getStates().size() ? MESSAGE_TEXT_STATE : MESSAGE_TEXT_TEST, msg.getText());
		}
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

}
