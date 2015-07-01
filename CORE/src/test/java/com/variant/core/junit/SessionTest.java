package com.variant.core.junit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.Variant;

public class SessionTest {

	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {
		
		// Bootstrap the Variant container with all defaults
		// and a simple session key resolver.
		Variant.Config config = new Variant.Config();
		config.getSessionServiceConfig().setKeyResolverClassName("com.variant.ext.session.SessionKeyResolverJunit");
		Variant.bootstrap(config);
	}

	/**
	 * 
	 */
	@Test
	public void basicTest() {
		System.out.println(Variant.getSession(false));
	}
}
