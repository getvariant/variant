package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.CommonError;
import com.variant.core.CoreException;
import com.variant.core.HookListener;
import com.variant.core.UserError.Severity;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedHook;
import com.variant.core.schema.TestParsedHook;
import com.variant.core.schema.parser.ParserMessageImpl;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.SchemaParser;

/**
 * TODO: Test parse time user hooks.
 * @author Igor
 * 
 */
public class ParserHookTest extends BaseTestCore {

	private static final String MESSAGE_TEXT_STATE = "Message-State";
	private static final String MESSAGE_TEXT_TEST = "Message-Test";
	
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
		assertNull(response.getSchema());
		assertEquals(11, listener.stateList.size());
		assertEquals(11, response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.ERROR, msg.getSeverity());
			assertEquals(new ParserMessageImpl(CommonError.HOOK_LISTENER_ERROR, MESSAGE_TEXT_STATE).getText(), msg.getText());			
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
		assertNull(response.getSchema());
		assertEquals(3, testListener.testList.size());
		assertEquals(3, response.getMessages().size());
		for (ParserMessage msg: response.getMessages()) {
			assertEquals(Severity.ERROR, msg.getSeverity());
			assertEquals(new ParserMessageImpl(CommonError.HOOK_LISTENER_ERROR, MESSAGE_TEXT_TEST).getText(), msg.getText());			
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
			hook.addMessage(MESSAGE_TEXT_STATE);
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
			hook.addMessage(MESSAGE_TEXT_TEST);
		}		
	}

}
