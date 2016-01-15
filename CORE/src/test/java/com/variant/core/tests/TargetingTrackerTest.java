package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.ext.TargetingTrackerString;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;

public class TargetingTrackerTest extends BaseTest {
	
	/**
	 * 
	 */
	@Test
	public void targetingTrackerStringTest() {
				
		ParserResponse response = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema = api.getSchema();
		
		VariantSession ssn = api.getSession("key1");
		assertNull(ssn.getStateRequest());

		long timestamp = System.currentTimeMillis();
		
		// 
		// Empty string
		//
		VariantTargetingTracker tp = new TargetingTrackerString();
		tp.initialized(ssn, "");
		assertEquals(0, tp.getAll().size());

		// 
		// Single entry - good
		//
		String persisterString = timestamp + ".test1.A";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		tp.remove(schema.getTest("test1"));
		assertEquals(0, tp.getAll().size());
		assertNull(tp.get(schema.getTest("test1")));

		persisterString = timestamp + ".test1.A|";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));

		persisterString = "|" + timestamp + ".test2.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test2.B"), tp.get(schema.getTest("test2")));

		persisterString = "|" + timestamp + ".test3.C|";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(1, tp.getAll().size());
		assertEquals(experience("test3.C"), tp.get(schema.getTest("test3")));
		assertNull(tp.get(schema.getTest("test1")));
		boolean exceptionThrown = false;
		try {
			assertNull(tp.get(null));
		}
		catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		exceptionThrown = false;
		try {
			assertNull(tp.remove(null));
		}
		catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		
		// 
		// Single entry - bad
		//
		persisterString = "|test2.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(0, tp.getAll().size());

		persisterString = timestamp + ".badTestName.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(0, tp.getAll().size());

		persisterString = "|" + timestamp + ".test3.badExperienceName|";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(0, tp.getAll().size());

		persisterString = "notANumber.test3.C|";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(0, tp.getAll().size());

		// 
		// Multiple entries - good
		//
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test4.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
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
		
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C||" + timestamp + ".test4.B|";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(3, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(experience("test4.B"), tp.get(schema.getTest("test4")));
		assertEquals(timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test4.B",tp.toString());

		// 
		// Multiple entries - bad
		//
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test4..B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(2, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(timestamp + ".test1.A|" + timestamp + ".test2.C",tp.toString());

		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test1.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(2, tp.getAll().size());
		assertEquals(experience("test1.B"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(timestamp + ".test1.B|" + timestamp + ".test2.C",tp.toString());

		// 
		// Idle Days To Live
		//
		timestamp = System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY;
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test3.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(3, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(experience("test3.B"), tp.get(schema.getTest("test3")));
		assertNull(tp.get(schema.getTest("test4")));

		timestamp = System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY - 1;
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test3.B";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(2, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertNull(tp.get(schema.getTest("test3")));
		assertNull(tp.get(schema.getTest("test4")));

		timestamp = System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY * 1000;
		persisterString = timestamp + ".test1.A|" + timestamp + ".test2.C|" + timestamp + ".test3.B|" + timestamp + ".test4.A|";
		tp = new TargetingTrackerString();
		tp.initialized(ssn, persisterString);
		assertEquals(3, tp.getAll().size());
		assertEquals(experience("test1.A"), tp.get(schema.getTest("test1")));
		assertEquals(experience("test2.C"), tp.get(schema.getTest("test2")));
		assertEquals(experience("test4.A"), tp.get(schema.getTest("test4")));
		assertNull(tp.get(schema.getTest("test3")));
		
	}

	
}
