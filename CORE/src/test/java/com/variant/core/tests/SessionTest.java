package com.variant.core.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantRuntimeException;
import com.variant.core.VariantSession;
import com.variant.core.session.TargetingPersister;
import com.variant.ext.session.SessionKeyResolverJunit;

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
	 * @throws VariantRuntimeException 
	 * 
	 */
	@Test
	public void basicTest() throws VariantBootstrapException, VariantRuntimeException {
		
		assertNull(Variant.getSession(false, new SessionKeyResolverJunit.UserDataImpl("foo")));
		VariantSession bar = Variant.getSession(true, new SessionKeyResolverJunit.UserDataImpl("bar"));
		assertNotNull(bar);
		
		VariantSession bar2 = Variant.getSession(false, new SessionKeyResolverJunit.UserDataImpl("bar"));
		assertEquals(bar, bar2);
		
		bar2 = Variant.getSession(true, new SessionKeyResolverJunit.UserDataImpl("bar"));
		assertEquals(bar, bar2);

		bar2 = Variant.getSession(new SessionKeyResolverJunit.UserDataImpl("bar"));
		assertEquals(bar, bar2);

		assertNull(bar.getTargetingPersister());
		bar.initTargetingPersister();
		TargetingPersister tp = bar.getTargetingPersister();
		assertEquals(0, tp.getAll().size());
	}
}
