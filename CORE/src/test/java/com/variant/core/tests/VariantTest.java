package com.variant.core.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.session.TargetingPersister;
import com.variant.core.util.SessionKeyResolverJunit.UserDataJunit;

public class VariantTest extends BaseTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeEachTest() throws Exception {

		// Bootstrap the Variant container with defaults.
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.core.util.SessionKeyResolverJunit");
		Variant.bootstrap(variantConfig);

	}

	@Test
	public void test() {
		
		ParserResponse response = Variant.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		VariantSession ssn = Variant.getSession(new UserDataJunit("sessioin-key"));
		
		TargetingPersister tp = ssn.getTargetingPersister();
		
	}
}
