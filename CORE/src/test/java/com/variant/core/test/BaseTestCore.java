 package com.variant.core.test;

import org.junit.Before;

import com.variant.core.impl.VariantCore;

/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends CoreBaseTest {
		
	private static Boolean sqlSchemaCreated = false;
	
	private VariantCore core;
		
	/**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 */
	@Before
	public void _beforeTest() throws Exception {
		synchronized (sqlSchemaCreated) { // once per JVM
			if (!sqlSchemaCreated) {
				rebootApi();
				recreateSchema(core);
				sqlSchemaCreated = true;
			}
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected VariantCore rebootApi() {
		core = new VariantCore("/variant-test.props");
		return core;
	}

}
