package com.variant.client.test.servlet;

import org.junit.experimental.categories.Category;

import com.variant.client.impl.VariantClientImpl;
import com.variant.client.servlet.adapter.VariantServletClient;
import com.variant.core.impl.VariantCore;
import com.variant.core.util.inject.Injector;

/**
 * Same thing as the superclass, but running remotely, against the real server,
 * as will be the case in practice. The remote session store is injected with
 * Injector.setConfigNameAsResource() method below.
 *
 */
@Category(Remote.class)
public class ServletSessionRemoteTest extends ServletSessionTest {

	private VariantCore core;
	
	@Override
	protected VariantServletClient newServletAdapterClient() {
		Injector.setConfigNameAsResource("/variant/injector-servlet-adapter-remote-test.json");
		VariantServletClient result = new VariantServletClient("/variant/servlet-adapter-test.props");
		core = ((VariantClientImpl)result.getBareClient()).getCoreApi();
		return result;
	}

	@Override
	protected VariantCore getCoreApi() {
		return core;
	}

}
