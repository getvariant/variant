package com.variant.core.tests;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.conf.VariantProperties;
import com.variant.core.event.BaseEvent;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.event.ViewServeEventTestFacade;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.schema.impl.ParserResponse;
import com.variant.ext.session.SessionKeyResolverJunit;

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
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.ext.session.SessionKeyResolverJunit");
		
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

		Schema config = Variant.getTestConfiguration();
		com.variant.core.schema.Test test = config.getTest("test1");
		View view = config.getView("view1");
		VariantSession ssn = Variant.getSession(new SessionKeyResolverJunit.UserDataImpl("foo"));
		ViewServeEventTestFacade event1 = new ViewServeEventTestFacade(view, ssn, BaseEvent.Status.SUCCESS, "viewResolvedPath");
		ViewServeEventTestFacade event2 = new ViewServeEventTestFacade(view, ssn, BaseEvent.Status.SUCCESS, "viewResolvedPath");
		event1.addExperience(test.getExperience("A"));
		event1.putParameter("event1-key1", "value1");
		event2.addExperience(test.getExperience("B"));
		event2.putParameter("event2-key1", "value1");
		event2.putParameter("event2-key2", "value2");
		Variant.getEventWriter().write(event1);
		Variant.getEventWriter().write(event2);

	}
	

}
