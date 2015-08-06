package com.variant.core.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantSession;
import com.variant.core.schema.Schema;
import com.variant.core.session.TargetingPersister;
import com.variant.core.session.TargetingPersisterFromString.UserDataFromString;
import com.variant.core.util.SessionKeyResolverJunit.UserDataJunit;
import com.variant.core.util.VariantJunitLogger;

public class SessionTest extends BaseTest {

	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {
		
		// Bootstrap the Variant container with all defaults
		// and a simple session key resolver.
		Variant.Config config = new Variant.Config();
		config.getSessionServiceConfig().setKeyResolverClassName("com.variant.core.util.SessionKeyResolverJunit");
		Variant.bootstrap(config);
		
	}

	/**
	 * 
	 */
	@Test
	public void sessionCreationTest() throws VariantBootstrapException {
		
		assertNull(Variant.getSession(false, new UserDataJunit("foo")));
		VariantSession bar = Variant.getSession(true, new UserDataJunit("bar"));
		assertNotNull(bar);
		
		VariantSession bar2 = Variant.getSession(false, new UserDataJunit("bar"));
		assertEquals(bar, bar2);
		
		bar2 = Variant.getSession(true, new UserDataJunit("bar"));
		assertEquals(bar, bar2);

		bar2 = Variant.getSession(new UserDataJunit("bar"));
		assertEquals(bar, bar2);
	}
	
	/**
	 * 
	 */
	@Test
	public void targetingPersosterFromStringTest() throws VariantBootstrapException {
				
		ParserResponse response = Variant.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());
		
		Schema schema = Variant.getSchema();
		VariantJunitLogger logger = (VariantJunitLogger) Variant.getLogger();
		
		VariantSession ssn = Variant.getSession(new UserDataJunit("key1"));
		long timestamp = System.currentTimeMillis();
		
		// 
		// Empty string
		//
		assertNull(ssn.getTargetingPersister());
		ssn.initTargetingPersister(new UserDataFromString(""));
		TargetingPersister tp = ssn.getTargetingPersister();
		assertEquals(0, tp.getAll().size());
		assertTrue(logger.get(-1).getLevel().lessThan(VariantJunitLogger.Level.WARN));
		
		// 
		// Single entry - good
		//
		String persisterString = timestamp + ".test1.A";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel().lessThan(VariantJunitLogger.Level.WARN));
		tp = ssn.getTargetingPersister();
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		tp.remove(schema.getTest("test1"));
		assertEquals(0, tp.getAll().size());
		assertNull(tp.get(schema.getTest("test1")));

		persisterString = timestamp + ".test1.A|";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel().lessThan(VariantJunitLogger.Level.WARN));
		tp = ssn.getTargetingPersister();
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));

		persisterString = "|" + timestamp + ".test2.B";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel().lessThan(VariantJunitLogger.Level.WARN));
		tp = ssn.getTargetingPersister();
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test2.B"), tp.get(schema.getTest("test2")));

		persisterString = "|" + timestamp + ".test3.C|";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel().lessThan(VariantJunitLogger.Level.WARN));
		tp = ssn.getTargetingPersister();
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test3.C"), tp.get(schema.getTest("test3")));

		// 
		// Single entry - bad
		//
		persisterString = "|test2.B";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel() == VariantJunitLogger.Level.DEBUG);
		assertEquals("Unable to parse entry [test2.B] for session [key1]", logger.get(-1).getMessage());
		tp = ssn.getTargetingPersister();
		assertEquals(0, tp.getAll().size());

		persisterString = timestamp + ".badTestName.B";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel() == VariantJunitLogger.Level.DEBUG);
		assertEquals("Ignored non-existent test [badTestName]", logger.get(-1).getMessage());
		tp = ssn.getTargetingPersister();
		assertEquals(0, tp.getAll().size());

		persisterString = "|" + timestamp + ".test3.badExperienceName|";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel() == VariantJunitLogger.Level.DEBUG);
		assertEquals("Ignored non-existent experience [test3.badExperienceName]", logger.get(-1).getMessage());
		tp = ssn.getTargetingPersister();
		assertEquals(0, tp.getAll().size());

		persisterString = "notANumber.test3.C|";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel() == VariantJunitLogger.Level.DEBUG);
		assertEquals("Unable to parse entry [notANumber.test3.C] for session [key1]", logger.get(-1).getMessage());
		assertTrue(logger.get(-1).getThrowable() instanceof NumberFormatException);
		tp = ssn.getTargetingPersister();
		assertEquals(0, tp.getAll().size());

		// 
		// Multiple entries - good
		//
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test4.B";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel().lessThan(VariantJunitLogger.Level.WARN));
		tp = ssn.getTargetingPersister();
		assertEquals(3, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(experience("test4.B"), tp.get(schema.getTest("test4")));
		tp.remove(schema.getTest("test2"));
		assertEquals(2, tp.getAll().size());
		assertNull(tp.get(schema.getTest("test2")));
		assertEquals(experience("test4.B"), tp.get(schema.getTest("test4")));
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(timestamp + ".test1.A|" + timestamp + ".test4.B",tp.toString());
		
		// 
		// Multiple entries - bad
		//
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test4..B";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		assertTrue(logger.get(-1).getLevel() == VariantJunitLogger.Level.DEBUG);
		assertEquals("Unable to parse entry [" + timestamp + ".test4..B] for session [key1]", logger.get(-1).getMessage());
		tp = ssn.getTargetingPersister();
		assertEquals(2, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(timestamp + ".test1.A|" + timestamp + ".test2.C",tp.toString());

	}

}
