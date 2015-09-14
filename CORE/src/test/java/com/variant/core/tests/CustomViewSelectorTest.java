package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.variant.core.StateSelector;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

/**
 * 
 * @author Igor
 *
 */
public class CustomViewSelectorTest extends BaseTest {
	
	@Test
	public void nullCustomViewTest() throws Exception {

		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());

		Schema schema = engine.getSchema();
		
		// Frist try null
		boolean exceptionThrown = false;	
		try {
			schema.registerCustomViewSelectorByPath(null);
		}
		catch (IllegalArgumentException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		
		// Default matcher uses exact match.
		assertNotNull(schema.matchViewByPath("/path/to/view5"));
		assertNull(schema.matchViewByPath("/path/TO/view5"));
		
		schema.registerCustomViewSelectorByPath(new CaseInsensitiveMatcher());
		assertNotNull(schema.matchViewByPath("/path/TO/view5"));
		assertNull(schema.matchViewByPath("/foo/bar"));
		
	}
	
	/**
	 * 
	 */
	private static class CaseInsensitiveMatcher implements StateSelector {

		@Override
		public State select(String path, Collection<State> views) {
			for (State v: views) {
				if (v.getPath().equalsIgnoreCase(path)) return v;
			}
			return null;
		}
		
	}
}
