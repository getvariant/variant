package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.schema.ViewSelectorByPath;

/**
 * 
 * @author Igor
 *
 */
public class CustomViewSelectorTest extends BaseTest {
	
	@Test
	public void nullCustomViewTest() throws Exception {

		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

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
	private static class CaseInsensitiveMatcher implements ViewSelectorByPath {

		@Override
		public View select(String path, Collection<View> views) {
			for (View v: views) {
				if (v.getPath().equalsIgnoreCase(path)) return v;
			}
			return null;
		}
		
	}
}
