package com.variant.share.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.share.schema.Schema;
import com.variant.share.schema.parser.ParserResponse;
import com.variant.share.schema.parser.SchemaParser;
import com.variant.share.session.CoreSession;
import com.variant.share.session.SessionScopedTargetingStabile;
import com.variant.share.util.IoUtils;

public class TargetingStabileTest extends BaseTestCore {
			
	/**
	 * 
	 */
	@Test
	public void targetingTrackerStringTest() throws Exception {
				
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(IoUtils.openResourceAsStream("/schema/monster.schema")._1());
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = response.getSchema();
		
		CoreSession ssn = new CoreSession("SID");
		assertFalse(ssn.getStateRequest().isPresent());
		
		// 
		// Empty string
		//
		setTargetingStabile(ssn, schema);
		assertEquals(0, ssn.getTargetingStabile().getAll().size());

		// 
		// Single entry
		//
		setTargetingStabile(ssn, schema, "test1.A");
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
		setTargetingStabile(ssn, schema, "test1.A", "test2.C", "test4.B");
		stabile = ssn.getTargetingStabile();
		assertEquals(3, stabile.getAll().size());
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience(schema, "test1.A")));
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience(schema, "test2.C")));
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience(schema, "test4.B")));

		assertNull(stabile.remove("junk"));
		assertEquals(3, stabile.getAll().size());
		
		assertNotNull(stabile.remove("test2"));
		assertEquals(2, stabile.getAll().size());
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience(schema, "test1.A")));
		assertFalse(stabile.getAllAsExperiences(schema).contains(experience(schema, "test2.C")));
		assertTrue(stabile.getAllAsExperiences(schema).contains(experience(schema, "test4.B")));

	}

	
}
