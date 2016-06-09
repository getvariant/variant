package com.variant.core.test;

import org.junit.Before;

import com.variant.core.impl.VariantCore;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;

/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends BaseTestCommon {
	
	protected VariantCore api = null;
	
	private static Boolean sqlSchemaCreated = false;
	
	/**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 */
	@Before
	public void _beforeTest() throws Exception {
		rebootApi(); // in each instance 
		synchronized (sqlSchemaCreated) { // once per JVM
			if (!sqlSchemaCreated) {
				recreateSchema();
				sqlSchemaCreated = true;
			}
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected void rebootApi() throws Exception {
		api = new VariantCore("/variant-test.props");
	}

	@Override
	protected JdbcService getJdbcService() {
		return new JdbcService(api);
	}

	@Override
	protected Schema getSchema() {
		return api.getSchema();
	}
}
