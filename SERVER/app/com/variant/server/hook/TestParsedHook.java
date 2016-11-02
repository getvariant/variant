package com.variant.server.hook;

import com.variant.core.xdm.Test;

/**
 * <p>Parse time hook. Posts its listeners whenever the schema parser completes parsing of a test. 
 * Will not post for a test if parse errors were encountered. Use this hook to enforce application 
 * semantics that is external to XDM, e.g. that a certain state parameter was supplied.
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
