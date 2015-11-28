package com.variant.core.flashpoint;

import com.variant.core.schema.Test;

/**
 * <p>Parse time flashpoint that is reached immediately after a test is successfully parsed.
 * This flashpoint will not be reached if errors were encountered during parsing of a test..
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface TestParsedFlashpoint extends ParserFlashpoint {

	/**
	 * Client code may obtain the test for which this flashpoint was reached.
	 * 
	 * @return An object of type {@link com.variant.core.schema.Test}.
     * @since 0.5
	 */
	public Test getTest();
}
