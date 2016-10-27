 package com.variant.core.test;

import com.variant.core.impl.VariantCore;
import com.variant.core.util.inject.Injector;

/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends VariantBaseTest {
		
	//private static Boolean sqlSchemaCreated = false;
		
	// Default, but can be overridden by subclass, if needed.
	protected static String injectorConfigAsResourceName = "/com/variant/core/conf/injector-session-store-local.json";

	/**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 * ON SERVER NOW
	@Before
	public void _beforeTest() throws Exception {
		synchronized (sqlSchemaCreated) { // once per JVM
			if (!sqlSchemaCreated) {
				VariantCore core = rebootApi();
				recreateSchema(core);
				sqlSchemaCreated = true;
			}
		}
	}
*/	
	/**
	 * 
	 */
	protected VariantCore rebootApi() {
		Injector.setConfigNameAsResource(injectorConfigAsResourceName);
		return new VariantCore("/com/variant/core/conf/test.props");
	}

}
