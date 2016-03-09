package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantPropertiesImpl;
import com.variant.core.impl.VariantPropertiesTestFacade;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.VariantIoUtils;
import com.variant.core.util.Tuples.*;

public class VariantPropertiesTest {

	@Test
	public void test() throws Exception {
		
		// Core default
		Variant api = Variant.Factory.getInstance();
		Properties expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));
		assertEquals(expectedProps.size(), VariantPropertiesImpl.Key.values().length);
		for (VariantPropertiesImpl.Key key: VariantPropertiesImpl.Key.values()) {
			assertEquals(expectedProps.getProperty(key.propName()), api.getProperties().get(key, String.class));
		}
		
		// Compile time override
		api = Variant.Factory.getInstance("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/variant-test.props"));
		VariantPropertiesTestFacade actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
			   "Property Name: [" + prop + "]", 
			   new Pair<String, String>(expectedProps.getProperty(prop), "/variant-test.props"), 
			   actualProps.getString(prop));
		}

		// Run time override from classpath
		final String RESOURCE_NAME = "/VariantPropertiesTest.props";
		System.setProperty(VariantPropertiesImpl.RUNTIME_PROPS_RESOURCE_NAME, RESOURCE_NAME);
		api = Variant.Factory.getInstance();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream(RESOURCE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvariant.props.resource=/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_RESOURCE_NAME);

		// Comp time override + run time override from classpath
		System.setProperty(VariantPropertiesImpl.RUNTIME_PROPS_RESOURCE_NAME, "/VariantPropertiesTest.props");
		api = Variant.Factory.getInstance("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/VariantPropertiesTest.props"));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvariant.props.resource=/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_RESOURCE_NAME);

		// Run time override from file system.
		final String TMP_FILE_NAME = "/tmp/VariantPropertiesTest.props";
		System.setProperty(VariantPropertiesImpl.RUNTIME_PROPS_FILE_NAME, TMP_FILE_NAME);
		PrintWriter tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantProperties.Key.TARGETING_TRACKER_CLASS_NAME.propName() + " = FileOverride");
		tmpFile.println(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName() + " = FileOverride");	
		tmpFile.close();
		
		api = Variant.Factory.getInstance();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvaraint.props.file=/tmp/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_RESOURCE_NAME);

		// Comp time override from class path + run time override from file system.
		System.setProperty(VariantPropertiesImpl.RUNTIME_PROPS_FILE_NAME, TMP_FILE_NAME);
		tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantProperties.Key.TARGETING_TRACKER_CLASS_NAME + " = FileOverride");
		tmpFile.println(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE + " = FileTimeOverride");	
		tmpFile.close();
		
		api = Variant.Factory.getInstance("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvaraint.props.file=/tmp/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_FILE_NAME);
		System.clearProperty(VariantPropertiesImpl.RUNTIME_PROPS_RESOURCE_NAME);
		
		// System props override. 
		int randomInt = new Random().nextInt();
		api = Variant.Factory.getInstance();
		assertNotEquals(randomInt, api.getProperties().get(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE,  Integer.class).intValue());
		System.setProperty(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName(), String.valueOf(randomInt));
		assertEquals(randomInt, api.getProperties().get(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE, Integer.class).intValue());

		api = Variant.Factory.getInstance();
		assertEquals(randomInt, api.getProperties().get(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE, Integer.class).intValue());
		System.clearProperty(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName());
		assertNotEquals(randomInt, api.getProperties().get(VariantProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE, Integer.class).intValue());
		
		// JSON parsing errors
		{
			// Invalid JSON
			final String BAD_JSON = "{\"foo\":\"FOO\"\"bar\":\"BAR\"}";
			System.setProperty(VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName(), BAD_JSON);
			boolean exceptionThrown = false;
			try {
				api = Variant.Factory.getInstance();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeException(
								MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON, 
								BAD_JSON, 
								VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
		}
		
		{
			// missing password
			System.setProperty(VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName(), "{\"url\":\"URL\",\"user\":\"USER\"}"); 
			boolean exceptionThrown = false;
			try {
				api = Variant.Factory.getInstance();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeException(
								MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET, 
								"password", 
								api.getProperties().get(VariantProperties.Key.EVENT_PERSISTER_CLASS_NAME, String.class),
								VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
		}		

	}

}
