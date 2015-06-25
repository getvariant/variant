package com.variant.core.session;

import java.util.Random;

import com.variant.core.util.StringUtils;

public class VariantSessionImplTestFacade extends VariantSessionImpl {

	private static Random random = new Random();
	
	@Override
	public String getId() {
		return StringUtils.random128BitString(random);
	}
}
