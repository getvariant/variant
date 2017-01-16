 package com.variant.core.test;

import com.variant.core.schema.Schema;


/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends VariantBaseTest {

	@Override
	protected Schema getSchema() {
		throw new RuntimeException("No schema in Core!");
	}

}
