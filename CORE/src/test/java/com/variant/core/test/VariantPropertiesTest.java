package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCore;
import com.variant.core.impl.CoreProperties;
import com.variant.core.impl.VariantPropertiesTestFacade;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantIoUtils;

public class VariantPropertiesTest {

	@Test
	public void test() throws Exception {
		
		// Core default
		VariantCore api = new VariantCore();
		
		Properties defaultProps = new Properties();
		defaultProps.load(VariantIoUtils.openResourceAsStream("/variant/defaults.props"));
		Properties internalProps = new Properties();
		internalProps.load(VariantIoUtils.openResourceAsStream("/variant/internal.7F1BDFD1F67FA313.props"));

		assertEquals(internalProps.size() + defaultProps.size(), CoreProperties.Key.values().length);

		for (CoreProperties.Key key: CoreProperties.Key.values()) {
			if (defaultProps.containsKey(key.propName())) 
				assertEquals(defaultProps.getProperty(key.propName()), api.getProperties().get(key, String.class));
			else
				assertEquals(internalProps.getProperty(key.propName()), api.getProperties().get(key, String.class));				
		}
		
		// Compile time override
		api = new VariantCore("/variant-test.props");
		Properties testProps = new Properties();
		testProps.load(VariantIoUtils.openResourceAsStream("/variant-test.props"));
		VariantPropertiesTestFacade actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: testProps.stringPropertyNames()) {
			assertEquals(
			   "Property Name: [" + prop + "]", 
			   new Pair<String, String>(testProps.getProperty(prop), "/variant-test.props"), 
			   actualProps.getString(prop));
		}

		// Run time override from classpath
		final String RESOURCE_NAME = "/VariantPropertiesTest.props";
		System.setProperty(CoreProperties.COMMANDLINE_RESOURCE_NAME, RESOURCE_NAME);
		api = new VariantCore();
		Properties expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream(RESOURCE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvariant.props.resource=/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(CoreProperties.COMMANDLINE_FILE_NAME);
		System.clearProperty(CoreProperties.COMMANDLINE_RESOURCE_NAME);

		// Comp time override + run time override from classpath
		System.setProperty(CoreProperties.COMMANDLINE_RESOURCE_NAME, "/VariantPropertiesTest.props");
		api = new VariantCore("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/VariantPropertiesTest.props"));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvariant.props.resource=/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(CoreProperties.COMMANDLINE_FILE_NAME);
		System.clearProperty(CoreProperties.COMMANDLINE_RESOURCE_NAME);

		// Run time override from file system.
		final String TMP_FILE_NAME = "/tmp/VariantPropertiesTest.props";
		System.setProperty(CoreProperties.COMMANDLINE_FILE_NAME, TMP_FILE_NAME);
		PrintWriter tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(CoreProperties.Key.TARGETING_TRACKER_CLASS_NAME.propName() + " = FileOverride");
		tmpFile.println(CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName() + " = FileOverride");	
		tmpFile.close();
		
		api = new VariantCore();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvaraint.props.file=/tmp/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(CoreProperties.COMMANDLINE_FILE_NAME);
		System.clearProperty(CoreProperties.COMMANDLINE_RESOURCE_NAME);

		// Comp time override from class path + run time override from file system.
		System.setProperty(CoreProperties.COMMANDLINE_FILE_NAME, TMP_FILE_NAME);
		tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(CoreProperties.Key.TARGETING_TRACKER_CLASS_NAME + " = FileOverride");
		tmpFile.println(CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE + " = FileTimeOverride");	
		tmpFile.close();
		
		api = new VariantCore("/variant-test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvaraint.props.file=/tmp/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(CoreProperties.COMMANDLINE_FILE_NAME);
		System.clearProperty(CoreProperties.COMMANDLINE_RESOURCE_NAME);
		
		// System props override. 
		int randomInt = new Random().nextInt();
		api = new VariantCore();
		assertNotEquals(randomInt, api.getProperties().get(CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE,  Integer.class).intValue());
		System.setProperty(CoreProperties.COMMANDLINE_PROP_PREFIX + CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName(), String.valueOf(randomInt));
		assertEquals(randomInt, api.getProperties().get(CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE, Integer.class).intValue());

		api = new VariantCore();
		assertEquals(randomInt, api.getProperties().get(CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE, Integer.class).intValue());
		System.clearProperty(CoreProperties.COMMANDLINE_PROP_PREFIX + CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName());
		assertNotEquals(randomInt, api.getProperties().get(CoreProperties.Key.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE, Integer.class).intValue());
		
		// JSON parsing errors
		{
			// Invalid JSON
			final String BAD_JSON = "{\"foo\":\"FOO\"\"bar\":\"BAR\"}";
			System.setProperty(CoreProperties.COMMANDLINE_PROP_PREFIX + CoreProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName(), BAD_JSON);
			boolean exceptionThrown = false;
			try {
				api = new VariantCore();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeException(
								MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON, 
								BAD_JSON, 
								CoreProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
		}
		
		{
			// missing password
			System.setProperty(CoreProperties.COMMANDLINE_PROP_PREFIX + CoreProperties.Key.EVENT_PERSISTER_CLASS_NAME.propName(), "com.variant.core.ext.EventPersisterH2"); 
			System.setProperty(CoreProperties.COMMANDLINE_PROP_PREFIX + CoreProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName(), "{\"url\":\"URL\",\"user\":\"USER\"}"); 
			boolean exceptionThrown = false;
			try {
				api = new VariantCore();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeException(
								MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET, 
								"password", 
								api.getProperties().get(CoreProperties.Key.EVENT_PERSISTER_CLASS_NAME, String.class),
								CoreProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
		}		

	}

}
