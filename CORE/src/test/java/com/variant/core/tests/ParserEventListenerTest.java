package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.exception.VariantException;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.TestParsedEventListener;
import com.variant.core.schema.parser.StateParsedEventListener;

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
public class ParserEventListenerTest extends BaseTest {

	@Test
	public void viewParsedListenerTest() throws Exception {
		Variant variant = Variant.Factory.getInstance();
		StateParsedEventListenerImpl listener = new StateParsedEventListenerImpl();
		variant.addListener(listener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());
		assertEquals(variant.getSchema().getStates(), listener.viewList);
		
	}

	@Test
	public void testParsedListenerTest() throws Exception {
		Variant variant = Variant.Factory.getInstance();
		TestParsedEventListenerImpl listener = new TestParsedEventListenerImpl();
		variant.addListener(listener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());
		assertEquals(variant.getSchema().getTests(), listener.testList);
		
	}

	/**
	 * 
	 */
	private static class StateParsedEventListenerImpl implements StateParsedEventListener {

		private ArrayList<State> viewList = new ArrayList<State>();
		
		@Override
		public void stateParsed(State view) throws VariantException {
			viewList.add(view);
		}		
	}
	
	/**
	 * 
	 */
	private static class TestParsedEventListenerImpl implements TestParsedEventListener {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		
		@Override
		public void testParsed(com.variant.core.schema.Test test) throws VariantException {
			testList.add(test);
		}		
	}

}
