package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantRuntimeException;
import com.variant.core.config.TestConfig;
import com.variant.core.config.View;
import com.variant.core.config.parser.ConfigParser;
import com.variant.core.config.parser.ParserResponse;
import com.variant.core.error.ErrorTemplate;


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
		
		ParserResponse response = ConfigParser.parse(ConfigParserHappyPathTest.CONFIG);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		final TestConfig config = Variant.getTestConfig();
		
		View view = config.getView("view1");
		try {
			assertFalse(view.isInvariantIn(config.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ }

		try {
			assertFalse(view.isInvariantIn(config.getTest("test2")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view1", "test2").getMessage(), vre.getMessage());
		}

		view = config.getView("view2");
		try {
			assertFalse(view.isInvariantIn(config.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view2", "test1").getMessage(), vre.getMessage());
		}
		try {
			assertFalse(view.isInvariantIn(config.getTest("Test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view2", "Test1").getMessage(), vre.getMessage());
		}

		view = config.getView("view4");
		try {
			assertFalse(view.isInvariantIn(config.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, "view4", "test1").getMessage(), vre.getMessage());
		}
		
		view = config.getView("view5");
		try {
			assertFalse(view.isInvariantIn(config.getTest("test1")));
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

		ParserResponse response = ConfigParser.parse(ConfigParserHappyPathTest.CONFIG);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		final TestConfig config = Variant.getTestConfig();
		
		try {
			config.getView("non-existent");
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeException(ErrorTemplate.RUN_NO_VIEW_FOR_PATH, "non-existent").getMessage(), vre.getMessage());
		}

	}
}

