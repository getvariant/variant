package com.variant.core.test;

import org.junit.Before;

import com.variant.core.VariantCoreSession;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.SessionScopedTargetingStabile;
import com.variant.core.impl.VariantCore;
import com.variant.core.impl.VariantCoreSessionTestFacade;
import com.variant.core.jdbc.JdbcService;

/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends BaseTestCommon {
		
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
		synchronized (sqlSchemaCreated) { // once per JVM
			if (!sqlSchemaCreated) {
				recreateSchema(rebootApi());
				sqlSchemaCreated = true;
			}
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected VariantCore rebootApi() {
		return new VariantCore("/variant-test.props");
	}

	@Override
	protected JdbcService getJdbcService(VariantCore core) {
		return new JdbcService(core);
	}

	/**
	 * @param ssn The session which will receive this stabile.
	 * @param experiences are expected as "test.exp" 
	 * @return
	 */
    protected void setTargetingStabile(VariantCoreSession ssn, String...experiences) {
		long timestamp = System.currentTimeMillis();
		SessionScopedTargetingStabile stabile = new SessionScopedTargetingStabile();
		for (String exp: experiences) stabile.add(experience(exp, ((CoreSessionImpl)ssn).getCoreApi()), timestamp);
		VariantCoreSessionTestFacade.setTargetingStabile((CoreSessionImpl)ssn, stabile);
	}


}
