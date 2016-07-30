package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

import com.variant.core.VariantCorePropertyKeys;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.impl.CorePropertiesImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.impl.VariantPropertiesTestFacade;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantIoUtils;

public class CorePropertiesTest {

	@Test
	public void test() throws Exception {
		
		VariantCore api = new VariantCore();
		
		// Compile time override
		api = new VariantCore("/com/variant/core/conf/test.props");
		Properties testProps = new Properties();
		testProps.load(VariantIoUtils.openResourceAsStream("/com/variant/core/conf/test.props"));
		VariantPropertiesTestFacade actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String propName: testProps.stringPropertyNames()) {
			assertEquals(
			   "Property Name: [" + propName + "]", 
			   new Pair<String, String>(testProps.getProperty(propName), "/com/variant/core/conf/test.props"), 
			   actualProps.getString(propName));
		}

		// Run time override from classpath
		final String RESOURCE_NAME = "/VariantPropertiesTest.props";
		System.setProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME, RESOURCE_NAME);
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
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME);
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME);

		// Comp time override + run time override from classpath
		System.setProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME, "/VariantPropertiesTest.props");
		api = new VariantCore("/com/variant/core/conf/test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/VariantPropertiesTest.props"));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvariant.props.resource=/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME);
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME);

		// Run time override from file system.
		final String TMP_FILE_NAME = "/tmp/VariantPropertiesTest.props";
		System.setProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME, TMP_FILE_NAME);
		PrintWriter tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_INIT.propertyName() + " = {'foo':'bar'}");
		tmpFile.println(VariantCorePropertyKeys.EVENT_WRITER_MAX_DELAY_MILLIS.propertyName() + " = 12345678");	
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
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME);
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME);

		// Comp time override from class path + run time override from file system.
		System.setProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME, TMP_FILE_NAME);
		tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantCorePropertyKeys.EVENT_WRITER_MAX_DELAY_MILLIS.propertyName() + " = 12345678");	
		tmpFile.close();
		
		api = new VariantCore("/com/variant/core/conf/test.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		actualProps = new VariantPropertiesTestFacade(api.getProperties());
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals(
					"Property Name: [" + prop + "]", 
					new Pair<String,String>(expectedProps.getProperty(prop), "-Dvaraint.props.file=/tmp/VariantPropertiesTest.props"), 
					actualProps.getString(prop));
		}
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME);
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME);
		
		// System props override. 
		int randomInt = new Random().nextInt();
		api = new VariantCore();
		assertNotEquals(randomInt, api.getProperties().get(VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE,  Integer.class).intValue());
		System.setProperty(CorePropertiesImpl.COMMANDLINE_PROP_PREFIX + VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE.propertyName(), String.valueOf(randomInt));
		assertEquals(randomInt, api.getProperties().get(VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE, Integer.class).intValue());

		api = new VariantCore();
		assertEquals(randomInt, api.getProperties().get(VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE, Integer.class).intValue());
		System.clearProperty(CorePropertiesImpl.COMMANDLINE_PROP_PREFIX + VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE.propertyName());
		assertNotEquals(randomInt, api.getProperties().get(VariantCorePropertyKeys.EVENT_WRITER_BUFFER_SIZE, Integer.class).intValue());
		
		// JSON parsing errors
		{
			// Invalid JSON
			final String BAD_JSON = "{\"foo\":\"FOO\"\"bar\":\"BAR\"}";
			System.setProperty(CorePropertiesImpl.COMMANDLINE_PROP_PREFIX + VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_INIT.propertyName(), BAD_JSON);
			boolean exceptionThrown = false;
			try {
				api = new VariantCore();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeUserErrorException(
								MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON, 
								BAD_JSON, 
								VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_INIT.propertyName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
		}
		
		{
			// missing password
			System.setProperty(CorePropertiesImpl.COMMANDLINE_PROP_PREFIX + VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_NAME.propertyName(), "com.variant.core.event.EventPersisterH2"); 
			System.setProperty(CorePropertiesImpl.COMMANDLINE_PROP_PREFIX + VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_INIT.propertyName(), "{\"url\":\"URL\",\"user\":\"USER\"}"); 
			boolean exceptionThrown = false;
			try {
				api = new VariantCore();
			}
			catch (VariantRuntimeException e) {
				exceptionThrown = true;
				assertEquals(
						new VariantRuntimeUserErrorException(
								MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET, 
								"password", 
								api.getProperties().get(VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_NAME, String.class),
								VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_INIT.propertyName()
								).getMessage(), 
						e.getMessage());
			}
			assertTrue(exceptionThrown);
		}		

	}

}
