package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantRuntimeException;
import com.variant.core.error.ErrorTemplate;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;


public class RuntimeExceptionTest extends BaseTest {
	
	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {
		
		// Bootstrap the Variant container with defaults.
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.ext.session.SessionKeyResolverJunit");
		Variant.bootstrap(variantConfig);
	}

	/**
	 * RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST
	 */
	@Test
	public void runViewNotInstrumentedForTest_Test() throws Exception {
		
		ParserResponse response = Variant.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		final Schema schema = Variant.getSchema();
		
		View view = schema.getView("view1");
		try {
			assertFalse(view.isInvariantIn(schema.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ }

		try {
			assertFalse(view.isInvariantIn(schema.getTest("test2")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view1", "test2").getMessage(), vre.getMessage());
		}

		view = schema.getView("view2");
		try {
			assertFalse(view.isInvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view2", "test1").getMessage(), vre.getMessage());
		}
		try {
			assertFalse(view.isInvariantIn(schema.getTest("Test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view2", "Test1").getMessage(), vre.getMessage());
		}

		view = schema.getView("view4");
		try {
			assertFalse(view.isInvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view4", "test1").getMessage(), vre.getMessage());
		}
		
		view = schema.getView("view5");
		try {
			assertFalse(view.isInvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view5", "test1").getMessage(), vre.getMessage());
		}

	}

	/**
	 * RUN_NO_VIEW_FOR_PATH
	 */
	@Test
	public void runNoViewForPath_Test() throws Exception {

		ParserResponse response = Variant.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		final Schema schema = Variant.getSchema();		
		assertNull(schema.getView("non-existent"));

	}
}

