package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.exception.VariantException;
import com.variant.core.schema.TestParsedEventListener;
import com.variant.core.schema.View;
import com.variant.core.schema.ViewParsedEventListener;

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
public class ParserEventListenerTest extends BaseTest {

	@Test
	public void viewParsedListenerTest() throws Exception {
		Variant variant = Variant.Factory.getInstance();
		ViewParsedEventListenerImpl listener = new ViewParsedEventListenerImpl();
		variant.addListener(listener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		assertEquals(variant.getSchema().getViews(), listener.viewList);
		
	}

	@Test
	public void testParsedListenerTest() throws Exception {
		Variant variant = Variant.Factory.getInstance();
		TestParsedEventListenerImpl listener = new TestParsedEventListenerImpl();
		variant.addListener(listener);
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		assertEquals(variant.getSchema().getTests(), listener.testList);
		
	}

	/**
	 * 
	 */
	private static class ViewParsedEventListenerImpl implements ViewParsedEventListener {

		private ArrayList<View> viewList = new ArrayList<View>();
		
		@Override
		public void viewParsed(View view) throws VariantException {
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
