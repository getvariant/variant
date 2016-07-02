package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;


public class CoreRuntimeExceptionTest extends BaseTestCore {
	
	private VariantCore core = rebootApi();

	/**
	 * RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST
	 */
	@Test
	public void runViewNotInstrumentedForTest_Test() throws Exception {
		
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		final Schema schema = core.getSchema();
		
		State view = schema.getState("state1");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ }

		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test2")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state1", "test2").getMessage(), vre.getMessage());
		}

		view = schema.getState("state2");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state2", "test1").getMessage(), vre.getMessage());
		}
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("Test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state2", "Test1").getMessage(), vre.getMessage());
		}

		view = schema.getState("state4");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state4", "test1").getMessage(), vre.getMessage());
		}
		
		view = schema.getState("state5");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state5", "test1").getMessage(), vre.getMessage());
		}

	}

	/**
	 * RUN_NO_VIEW_FOR_PATH
	 */
	@Test
	public void runNoViewForPath_Test() throws Exception {

		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		final Schema schema = core.getSchema();		
		assertNull(schema.getState("non-existent"));

	}
}

