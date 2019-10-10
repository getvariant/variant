 package com.variant.core.test;

import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.schema.parser.SchemaParserServerless;


/**
 * Base class for all Core JUnit tests.
 */
public class BaseTestCore extends VariantBaseTest {	
	
	/**
	 * Core tests will use a concrete schema parser that operates on a null hooks service.
	 * This is good enough for the client side parsing. Server will test parse time hooks.
	 * 
	 * @return
	 */
	protected SchemaParser getSchemaParser() {
		return new SchemaParserServerless();
	}
}
