package com.variant.core.tests;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantEvent.Status;
import com.variant.core.VariantSession;
import com.variant.core.conf.VariantProperties;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.runtime.ViewServeEventTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.util.SessionKeyResolverJunit;
import com.variant.core.util.VariantCollectionsUtils;

public class EventWriterTest extends BaseTest {

	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeEachTest() throws Exception {

		// Bootstrap the Variant container
		
		EventWriter.Config eventWriterConfig = new EventWriter.Config();
		eventWriterConfig.setMaxPersisterIntervalMillis(100);
		eventWriterConfig.setBufferSize(10);
		
		EventPersister.Config eventPersisterConfig = new EventPersister.Config();
		eventPersisterConfig.setJdbcUrl(VariantProperties.jdbcUrl()); 
		eventPersisterConfig.setJdbcUser(VariantProperties.jdbcUser());
		eventPersisterConfig.setJdbcPassword(VariantProperties.jdbcPassword());
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.setEventPersisterClassName(VariantProperties.persisterClassName());
		variantConfig.setEventPersisterConfig(eventPersisterConfig);
		variantConfig.setEventWriterConfig(eventWriterConfig);
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.core.util.SessionKeyResolverJunit");
		
		Variant.bootstrap(variantConfig);

		// (Re)create the schema;
		switch (VariantProperties.jdbcVendor()) {
		case POSTGRES: 
			JdbcUtil.recreateSchema();
			break;
		case H2:
			JdbcUtil.createSchema();  // Fresh in-memory DB.
			break;
		}

	}

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

		
		ParserResponse response = Variant.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		Schema schema = Variant.getSchema();
		com.variant.core.schema.Test test = schema.getTest("test1");
		View view = schema.getView("view1");
		VariantSession ssn = Variant.getSession(new SessionKeyResolverJunit.UserDataJunit("session-key"));
		ViewServeEventTestFacade event1 = new ViewServeEventTestFacade(view, ssn, "viewResolvedPath", VariantCollectionsUtils.list(test.getExperience("A")));
		ViewServeEventTestFacade event2 = new ViewServeEventTestFacade(view, ssn, "viewResolvedPath", VariantCollectionsUtils.list(test.getExperience("B")));
		event1.setParameter("event1-key1", "value1");
		event2.setParameter("event2-key1", "value1");
		event2.setParameter("event2-key2", "value2"); 
		event2.setStatus(Status.SUCCESS);
		Variant.getEventWriter().write(event1);
		Variant.getEventWriter().write(event2);

	}
	

}
