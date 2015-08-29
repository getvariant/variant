package com.variant.core.tests;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.impl.ViewServeEventTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.util.VariantCollectionsUtils;

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
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		Schema schema = engine.getSchema();
		com.variant.core.schema.Test test = schema.getTest("test1");
		View view = schema.getView("view1");
		VariantSession ssn = engine.getSession("session-key");
		ssn.initTargetingPersister("");
		VariantViewRequest request = engine.startViewRequest(ssn, view);
		request.setStatus(VariantViewRequest.Status.FAIL);
		ViewServeEventTestFacade event1 = new ViewServeEventTestFacade(request, "viewResolvedPath", VariantCollectionsUtils.list(test.getExperience("A")));
		ViewServeEventTestFacade event2 = new ViewServeEventTestFacade(request, "viewResolvedPath", VariantCollectionsUtils.list(test.getExperience("B")));
		event1.setParameter("event1-key1", "value1");
		event2.setParameter("event2-key1", "value1");
		event2.setParameter("event2-key2", "value2"); 
		engine.getEventWriter().write(event1);
		engine.getEventWriter().write(event2);

	}
	

}
