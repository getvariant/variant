package com.variant.core.hook;

import com.variant.core.xdm.Test;

/**
 * <p>Parse time user hook that posts its listeners immediately after a test is successfully parsed.
 * This hook will not post for a test if errors were encountered during parsing of that test.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface TestParsedHook extends ParserHook {

	/**
	 * The test for which this hook is posting. It is safe to assume that no errors were
	 * encountered during parsing of this test.
	 * 
	 * @return An object of type {@link com.variant.core.xdm.Test}.
     * @since 0.5
	 */
	public Test getTest();
}
