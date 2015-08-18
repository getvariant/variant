package com.variant.core.impl;

import org.slf4j.Logger;

import com.variant.core.Variant;

public class VariantCoreImplTestFacade {

	public static void setLogger(Logger logger) {
		((VariantCoreImpl) Variant.Factory.getInstance()).setLogger(logger);
	}
}
