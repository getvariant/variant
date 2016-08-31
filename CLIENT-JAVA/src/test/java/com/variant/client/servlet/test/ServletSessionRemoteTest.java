package com.variant.client.servlet.test;

import org.junit.experimental.categories.Category;

import com.variant.client.impl.VariantClientImpl;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.impl.ServletClientImpl;
import com.variant.core.impl.VariantCore;

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
		//Injector.setConfigNameAsResource("/variant/injector-servlet-adapter-remote-test.json");  This is the default.
		ServletClientImpl result = (ServletClientImpl) VariantServletClient.Factory.getInstance("/variant/servlet-adapter-test.props");
		core = ((VariantClientImpl)result.getBareClient()).getCoreApi();
		return result;
	}

	@Override
	protected VariantCore getCoreApi() {
		return core;
	}

}