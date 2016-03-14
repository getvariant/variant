package com.variant.web.test;

import com.variant.web.VariantWeb;
import com.variant.webnative.test.SessionTest;

public class ThinSessionTest extends SessionTest {

	/**
	 * 
	 */
	@Override
	protected VariantWeb rebootApi() {
		return new VariantWeb("/variant-test.props");
	}

}
