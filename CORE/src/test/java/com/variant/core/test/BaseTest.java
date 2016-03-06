package com.variant.core.test;

import org.junit.Before;

import com.variant.core.Variant;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;

/**
 * Common utility methods for all JUnit tests.
 */

public class BaseTest extends BaseTestCommon {
	
	protected static VariantCoreImpl api = null;
	protected static JdbcService jdbc = null;
	
	private static Boolean beforeTestCaseRan = false;
	
	/**
	 * This should really be a @BeforeClass, but we can't do that
	 * because it would have to be static and we wouldn't be able to invoke
	 * instance methods we inherited from BaseTestCommon. Hence we
	 * make sure it only runs once.
	 * 
	 * @throws Exception
	 */
	@Before
	public void _beforeTestCase() throws Exception {
		synchronized (beforeTestCaseRan) {
		if (!beforeTestCaseRan) {
			rebootApi();
			recreateSchema();
			beforeTestCaseRan = true;
		}
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected static void rebootApi() throws Exception {
		api = (VariantCoreImpl) Variant.Factory.getInstance("/variant-test.props");
		jdbc = new JdbcService(api);
	}

	@Override
	protected JdbcService getJdbcService() {
		return jdbc;
	}

	@Override
	protected Schema getSchema() {
		return api.getSchema();
	}


}
