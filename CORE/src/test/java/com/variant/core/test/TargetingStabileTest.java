package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.impl.SessionId;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionScopedTargetingStabile;

public class TargetingStabileTest extends BaseTestCore {
		
	/**
	 * 
	 */
	@Test
	public void targetingTrackerStringTest() throws Exception {
				
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = response.getSchema();
		
		CoreSession ssn = new CoreSession(new SessionId("SID"), schema);
		assertNull(ssn.getStateRequest());
		
		// 
		// Empty string
		//
		setTargetingStabile(ssn);
		assertEquals(0, ssn.getTargetingStabile().getAll().size());

		// 
		// Single entry
		//
		setTargetingStabile(ssn, "test1.A");
		SessionScopedTargetingStabile stabile = ssn.getTargetingStabile();
		assertEquals(1, stabile.getAll().size());
		assertEquals(1, stabile.getAllAsExperiences(schema).size());
		assertEquals("test1", stabile.get("test1").getTestName());
		assertEquals("A", stabile.get("test1").getExperienceName());
		assertNull(stabile.get("junk"));
		assertNull(stabile.get(null));
		assertNotNull(stabile.remove("test1"));
		assertEquals(0, stabile.getAll().size());
		assertEquals(0, stabile.getAllAsExperiences(schema).size());
		assertNull(stabile.get("test1"));
		assertNull(stabile.remove(null));

		// 
		// Multiple entries - good
		//
		setTargetingStabile(ssn, "test1.A", "test2.C", "test4.B");
		stabile = ssn.getTargetingStabile();
		assertEquals(3, stabile.getAll().size());
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience("test1.A", schema)));
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience("test2.C", schema)));
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience("test4.B", schema)));

		assertNull(stabile.remove("junk"));
		assertEquals(3, stabile.getAll().size());
		
		assertNotNull(stabile.remove("test2"));
		assertEquals(2, stabile.getAll().size());
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience("test1.A", schema)));
		assertFalse(stabile.getAllAsExperiences(schema).contains(experience("test2.C", schema)));
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience("test4.B", schema)));

	}

	
}
