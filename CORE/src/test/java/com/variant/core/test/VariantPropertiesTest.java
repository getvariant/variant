package com.variant.core.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.config.VariantProperties;
import com.variant.core.config.VariantPropertiesTestFacade;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.VariantIoUtils;

public class VariantPropertiesTest {

	@Test
	public void test() throws Exception {

		Variant engine = Variant.Factory.getInstance();
		
		// Core default
		engine.bootstrap();
		Properties expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));
		assertEquals(expectedProps.size(), VariantProperties.Keys.values().length);
		for (VariantProperties.Keys key: VariantProperties.Keys.values()) {
			assertEquals(expectedProps.getProperty(key.propName()), key.propValue());
		}
		engine.shutdown();
		
		// Compile time override
		engine.bootstrap("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/variant-test.props"));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		engine.shutdown();

		// Run time override from classpath
		final String RESOURCE_NAME = "/VariantPropertiesTest.props";
		System.setProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME, RESOURCE_NAME);
		engine.bootstrap();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream(RESOURCE_NAME));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		engine.shutdown();
		System.clearProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME);

		// Comp time override + run time override from classpath
		System.setProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME, "/VariantPropertiesTest.props");
		engine.bootstrap("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/VariantPropertiesTest.props"));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		engine.shutdown();
		System.clearProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME);

		// Run time override from file system.
		final String TMP_FILE_NAME = "/tmp/VariantPropertiesTest.props";
		System.setProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME, TMP_FILE_NAME);
		PrintWriter tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantProperties.Keys.TARGETING_TRACKER_CLASS_NAME + " = FileOverride");
		tmpFile.println(VariantProperties.Keys.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE + " = RunTimeOverride");	
		tmpFile.close();
		
		engine.bootstrap();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		engine.shutdown();
		System.clearProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME);

		// Comp time override from class path + run time override from file system.
		System.setProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME, TMP_FILE_NAME);
		tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantProperties.Keys.TARGETING_TRACKER_CLASS_NAME + " = FileOverride");
		tmpFile.println(VariantProperties.Keys.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE + " = RunTimeOverride");	
		tmpFile.close();
		
		engine.bootstrap("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		engine.shutdown();
		System.clearProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME);
		
		// System props override. 
		int randomInt = new Random().nextInt();
		engine.bootstrap();
		assertNotEquals(randomInt, VariantProperties.getInstance().targetingTrackerIdleDaysToLive());
		System.setProperty(VariantProperties.Keys.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName(), String.valueOf(randomInt));
		assertEquals(randomInt, VariantProperties.getInstance().targetingTrackerIdleDaysToLive());
		engine.shutdown();
		engine.bootstrap();
		assertEquals(randomInt, VariantProperties.getInstance().targetingTrackerIdleDaysToLive());
		System.clearProperty(VariantProperties.Keys.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName());
		assertNotEquals(randomInt, VariantProperties.getInstance().targetingTrackerIdleDaysToLive());
		engine.shutdown();
		
		// JSON parsing errors
		{
			// Invalid JSON
			final String BAD_JSON = "{\"foo\":\"FOO\"\"bar\":\"BAR\"}";
			System.setProperty(VariantProperties.Keys.EVENT_PERSISTER_CLASS_INIT.propName(), BAD_JSON);
			boolean exceptionThrown = false;
			try {
				engine.bootstrap();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeException(
								MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON, 
								BAD_JSON, 
								VariantProperties.Keys.EVENT_PERSISTER_CLASS_INIT.propName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
			assertFalse(engine.isBootstrapped());
		}
		
		{
			// missing password
			System.setProperty(VariantProperties.Keys.EVENT_PERSISTER_CLASS_INIT.propName(), "{\"url\":\"URL\",\"user\":\"USER\"}"); 
			boolean exceptionThrown = false;
			try {
				engine.bootstrap();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeException(
								MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET, 
								"password", 
								VariantProperties.getInstance().eventPersisterClassName(),
								VariantProperties.Keys.EVENT_PERSISTER_CLASS_INIT.propName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
			assertFalse(engine.isBootstrapped());
		}		

	}

}
