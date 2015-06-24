package com.variant.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.variant.core.config.View;
import com.variant.core.config.parser.ViewTestImpl;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.event.ViewServeEvent;
import com.variant.core.session.VariantSessionTestImpl;
import com.variant.core.util.JdbcUtil;
import com.variant.core.util.TestProperties;

public class EventWriterTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeEachTest() throws Exception {

		// Bootstrap the Variant container
		
		EventWriter.Config eventWriterConfig = new EventWriter.Config();
		eventWriterConfig.setMaxPersisterIntervalMillis(100);
		eventWriterConfig.setBufferSize(10);
		
		EventPersister.Config persisterConfig = new EventPersister.Config();
		persisterConfig.setJdbcUrl(TestProperties.jdbcUrl()); 
		persisterConfig.setJdbcUser(TestProperties.jdbcUser());
		persisterConfig.setJdbcPassword(TestProperties.jdbcPassword());
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.setPersisterClassName(TestProperties.persisterClassName());
		variantConfig.setPersisterConfig(persisterConfig);
		variantConfig.setEventWriterConfig(eventWriterConfig);
		
		Variant.bootstrap(variantConfig);

		// (Re)create the schema;
		JdbcUtil.createSchema();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@After
	public void afterEachTest() throws Exception {
		// Sleep bit to give the event writer thread a chance to complete.
		Thread.sleep(1000);
	}
	
	@Test
	public void performanceTest() throws Exception {

		View view = new ViewTestImpl("view1", "/path/to/view1");
		VariantSession session = new VariantSessionTestImpl();
		
		ViewServeEvent event = new ViewServeEvent(view, session, ViewServeEvent.Status.SUCCESS, "/resolved/path/to/view1");
		
		Variant.getEventWriter().write(event);
	}
	

}
