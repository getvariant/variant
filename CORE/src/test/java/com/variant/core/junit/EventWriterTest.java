package com.variant.core.junit;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.config.TestConfig;
import com.variant.core.config.View;
import com.variant.core.config.parser.ConfigParser;
import com.variant.core.config.parser.ParserResponse;
import com.variant.core.event.BaseEvent;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.event.ViewServeEventTestFacade;
import com.variant.core.session.VariantSessionImplTestFacade;
import com.variant.core.util.JdbcUtil;
import com.variant.core.util.TestProperties;

public class EventWriterTest extends BaseTest {

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

		// Recreate the schema;
		JdbcUtil.recreateSchema();

	}

	/**
	 * 
	 * @throws Exception
	 */
	@After
	public void afterEachTest() throws Exception {

		// Sleep bit to give the event writer thread a chance to complete.
		Thread.sleep(2000);
	}
	
	@Test
	public void performanceTest() throws Exception {

		
		ParserResponse response = ConfigParser.parse(ConfigParserHappyPathTest.CONFIG);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		TestConfig config = Variant.getTestConfig();
		com.variant.core.config.Test test = config.getTest("test1");
		View view = config.getView("view1");
		VariantSession session = new VariantSessionImplTestFacade();
		ViewServeEventTestFacade event = new ViewServeEventTestFacade(view, session, BaseEvent.Status.SUCCESS, "viewResolvedPath");
		event.addExperience(test.getExperience("A"));
		Variant.getEventWriter().write(event);
	}
	

}
