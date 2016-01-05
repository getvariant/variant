package com.variant.core.hook;

import com.variant.core.schema.Test;

/**
 * <p>Parse time user hook that is reached immediately after a test is successfully parsed.
 * This hook will not be reached if errors were encountered during parsing of a test..
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface TestParsedHook extends ParserHook {

	/**
	 * Client code may obtain the test for which this hook was reached.
	 * 
	 * @return An object of type {@link com.variant.core.schema.Test}.
     * @since 0.5
	 */
	public Test getTest();
}
