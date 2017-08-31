 package com.variant.core.test;

import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.SchemaParser;


/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends VariantBaseTest {

	@Override
	protected Schema getSchema() {
		throw new RuntimeException("No schema in Core!");
	}
	
	
	/**
	 * Core tests will use a concrete schema parser that operates on a null user hooker.
	 * This is good enough for the client side parsing. Server will test parse time hooks.
	 * 
	 * @return
	 */
	protected SchemaParser getSchemaParser() {
		return new SchemaParser() {
			@Override
			protected UserHooker getHooker() {
				return UserHooker.NULL;
			}
		};
	}
}
