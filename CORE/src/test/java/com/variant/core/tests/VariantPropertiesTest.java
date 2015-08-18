package com.variant.core.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.VariantPropertiesTestFacade;
import com.variant.core.util.VariantIoUtils;

public class VariantPropertiesTest {

	@Test
	public void test() throws Exception {

		// Core default
		Variant.bootstrap();
		Properties expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));
		assertEquals(expectedProps.size(), VariantProperties.Keys.values().length);
		for (VariantProperties.Keys key: VariantProperties.Keys.values()) {
			assertEquals(expectedProps.getProperty(key.propName()), key.propValue());
		}
		Variant.shutdown();

		// Compile time override
		Variant.bootstrap("/variant-junit.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/variant-junit.props"));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		Variant.shutdown();

		// Run time override from classpath
		final String RESOURCE_NAME = "/VariantPropertiesTest.props";
		System.setProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME, RESOURCE_NAME);
		Variant.bootstrap();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream(RESOURCE_NAME));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		Variant.shutdown();

		// Comp time override + run time override from classpath
		System.setProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME, "/VariantPropertiesTest.props");
		Variant.bootstrap("/variant-junit.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openResourceAsStream("/VariantPropertiesTest.props"));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		Variant.shutdown();

		// Run time override from file system.
		final String TMP_FILE_NAME = "/tmp/VariantPropertiesTest.props";
		System.setProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME, TMP_FILE_NAME);
		PrintWriter tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantProperties.Keys.EVENT_PERSISTER_JDBC_URL + " = FileOverride");
		tmpFile.println(VariantProperties.Keys.EVENT_PERSISTER_JDBC_PASSWORD + " = RunTimeOverride");	
		tmpFile.close();
		
		Variant.bootstrap();
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		Variant.shutdown();

		// Comp tiie override from class path + run time override from file system.
		System.setProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME, TMP_FILE_NAME);
		tmpFile = new PrintWriter(new File(TMP_FILE_NAME));
		tmpFile.println(VariantProperties.Keys.EVENT_PERSISTER_JDBC_URL + " = FileOverride");
		tmpFile.println(VariantProperties.Keys.EVENT_PERSISTER_JDBC_PASSWORD + " = RunTimeOverride");	
		tmpFile.close();
		
		Variant.bootstrap("/variant-junit.props");
		expectedProps = new Properties();
		expectedProps.load(VariantIoUtils.openFileAsStream(TMP_FILE_NAME));
		for (String prop: expectedProps.stringPropertyNames()) {
			assertEquals("Property Name: [" + prop + "]", expectedProps.getProperty(prop), VariantPropertiesTestFacade.getString(prop));
		}
		Variant.shutdown();

	}

}
