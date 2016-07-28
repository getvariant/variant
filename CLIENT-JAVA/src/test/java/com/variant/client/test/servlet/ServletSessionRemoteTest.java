package com.variant.client.test.servlet;

import org.junit.experimental.categories.Category;

import com.variant.client.VariantClient;
import com.variant.core.util.inject.Injector;

/**
 * Same thing as the superclass, but running remotely, against the real server,
 * as will be the case in practice. The remote session store is injected with
 * Injector.setConfigNameAsResource() method below.
 *
 */
@Category(Remote.class)
public class ServletSessionRemoteTest extends ServletSessionTest {

	/**
	 * Override to inject the remote session store
	 */
	@Override
	protected VariantClient rebootApi() {
		Injector.setConfigNameAsResource("/variant/injector-servlet-adapter-remote-test.json");
		return VariantClient.Factory.getInstance("/variant/servlet-adapter-test.props");
	}

}
