package com.variant.core.tests;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;

import org.junit.After;
import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.impl.StateServeEventTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

public class EventWriterTest extends BaseTest {

	/**
	 * 
	 * @throws Exception
	 */
	@After
	public void afterEachTest() throws Exception {

		// Sleep a bit to give the event writer thread a chance to flush before JUnit kills it.
		Thread.sleep(2000);
	}
	
	@Test
	public void performanceTest() throws Exception {

		
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());

		long timestamp = System.currentTimeMillis();
		Schema schema = engine.getSchema();
		State state = schema.getState("state1");
		VariantSession ssn = engine.getSession("foo");
		VariantStateRequest request = engine.newStateRequest(ssn, state, timestamp + ".state1.A");
		HashMap<String,String> params = new HashMap<String, String>() {{put("path", "viewResolvedPath");}};
		StateServeEventTestFacade event1 = new StateServeEventTestFacade(request, params);
		request = engine.newStateRequest(ssn, state, timestamp + ".state1.B");
		StateServeEventTestFacade event2 = new StateServeEventTestFacade(request, params);
		event1.setParameter("event1-key1", "value1");
		event2.setParameter("event2-key1", "value1");
		event2.setParameter("event2-key2", "value2"); 
		engine.getEventWriter().write(event1);
		engine.getEventWriter().write(event2);

	}
	

}
