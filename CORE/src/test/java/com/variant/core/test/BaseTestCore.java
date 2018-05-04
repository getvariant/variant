 package com.variant.core.test;

import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.HooksService;
import com.variant.core.schema.parser.SchemaParser;


/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends VariantBaseTest {	
	
	/**
	 * Core tests will use a concrete schema parser that operates on a null user hooker.
	 * This is good enough for the client side parsing. Server will test parse time hooks.
	 * 
	 * @return
	 */
	protected SchemaParser getSchemaParser() {
		return new SchemaParser() {
			@Override
			public HooksService getHooksService() {
				return HooksService.NULL;
			}

			@Override
			public FlusherService getFlusherService() {
				return FlusherService.NULL;
			}
		};
	}
}
