package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.schema.parser.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;


public class RuntimeExceptionTest extends BaseTest {
	
	/**
	 * RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST
	 */
	@Test
	public void runViewNotInstrumentedForTest_Test() throws Exception {
		
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());
		final Schema schema = engine.getSchema();
		
		View view = schema.getView("view1");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ }

		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test2")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view1", "test2").getMessage(), vre.getMessage());
		}

		view = schema.getView("view2");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view2", "test1").getMessage(), vre.getMessage());
		}
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("Test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view2", "Test1").getMessage(), vre.getMessage());
		}

		view = schema.getView("view4");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view4", "test1").getMessage(), vre.getMessage());
		}
		
		view = schema.getView("view5");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view5", "test1").getMessage(), vre.getMessage());
		}

	}

	/**
	 * RUN_NO_VIEW_FOR_PATH
	 */
	@Test
	public void runNoViewForPath_Test() throws Exception {

		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());
		final Schema schema = engine.getSchema();		
		assertNull(schema.getView("non-existent"));

	}
}

