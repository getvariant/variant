package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.typesafe.config.Config;
import com.variant.core.CommonError;
import com.variant.core.conf.ConfigLoader;

public class ConfigTest extends BaseTestCore {

	@Test
	public void exceptionseTest() throws Exception {

		System.clearProperty("variant.config.resource");
		System.clearProperty("variant.config.file");
		System.clearProperty("variant.foo");

		// No Default is a problem
		new CoreInternalExceptionInterceptor() { 
			@Override public void toRun() {
				ConfigLoader.load("foo.bar", "bad.conf");
			}
		}.assertThrown("Could not find default config resource [bad.conf]");

		// File override must exist
		System.setProperty("variant.config.file", "non-existent");
		System.clearProperty("variant.config.resource");
		System.clearProperty("variant.config.resource");
		
		new CoreUserExceptionInterceptor() { 
			@Override public void toRun() {
				ConfigLoader.load("variant.conf", "variant-default.conf");
			}
		}.assertThrown(CommonError.CONFIG_FILE_NOT_FOUND, "non-existent");
		
		// Path override must exist
		System.setProperty("variant.config.resource", "non-existent");
		System.clearProperty("variant.config.file");
		
		new CoreUserExceptionInterceptor() { 
			@Override public void toRun() {
				ConfigLoader.load("variant.conf", "variant-default.conf");
			}
		}.assertThrown(CommonError.CONFIG_RESOURCE_NOT_FOUND, "non-existent");

	}

	@Test
	public void defaultConfigFileTest() {
		
		System.clearProperty("variant.config.resource");
		System.clearProperty("variant.config.file");
		System.clearProperty("variant.foo");
		
		// No resource is not a problem
		Config config = ConfigLoader.load("bad.conf", "variant-default.conf");
		assertEquals(2, config.entrySet().size());
		assertEquals("foo", config.getString("variant.foo"));
		assertFalse(config.hasPath("variant.bar"));
		assertEquals(7, config.getInt("variant.seven"));
		
		// Override defaults with existing resource.
		config = ConfigLoader.load("variant.conf", "variant-default.conf");
		assertEquals(3, config.entrySet().size());
		assertEquals("foo", config.getString("variant.foo"));
		assertEquals("bar", config.getString("variant.bar"));
		assertEquals(8, config.getInt("variant.seven"));
	}
	
	@Test
	public void overrideFromPathTest() {

		System.setProperty("variant.config.resource", "variant-path-override.conf");
		System.clearProperty("variant.config.file");
		System.clearProperty("variant.foo");
		
		// No resource
		Config config = ConfigLoader.load("bad.conf", "variant-default.conf");
		assertEquals(4, config.entrySet().size());
		assertEquals("bar", config.getString("variant.foo"));
		assertFalse(config.hasPath("variant.bar"));
		assertEquals(9, config.getInt("variant.seven"));
		assertTrue(config.getBoolean("overridden.from.path"));
		assertFalse(config.getBoolean("overridden.from.file"));
		
		// With resource
		config = ConfigLoader.load("variant.conf", "variant-default.conf");
		assertEquals(5, config.entrySet().size());
		assertEquals("bar", config.getString("variant.foo"));
		assertEquals("bar", config.getString("variant.bar"));
		assertEquals(9, config.getInt("variant.seven"));
		assertTrue(config.getBoolean("overridden.from.path"));
		assertFalse(config.getBoolean("overridden.from.file"));
	}

	@Test
	public void overrideFromFileTest() {

		System.setProperty("variant.config.file", "src/test/resources/variant-file-override.conf");
		System.clearProperty("variant.config.resource");
		System.clearProperty("variant.foo");
		
		// No resource
		Config config = ConfigLoader.load("bad.conf", "variant-default.conf");
		assertEquals(4, config.entrySet().size());
		assertEquals("bar", config.getString("variant.foo"));
		assertFalse(config.hasPath("variant.bar"));
		assertEquals(9, config.getInt("variant.seven"));
		assertFalse(config.getBoolean("overridden.from.path"));
		assertTrue(config.getBoolean("overridden.from.file"));
		
		// With resource
		config = ConfigLoader.load("variant.conf", "variant-default.conf");
		assertEquals(5, config.entrySet().size());
		assertEquals("bar", config.getString("variant.foo"));
		assertEquals("bar", config.getString("variant.bar"));
		assertEquals(9, config.getInt("variant.seven"));
		assertFalse(config.getBoolean("overridden.from.path"));
		assertTrue(config.getBoolean("overridden.from.file"));
	}

	@Test
	public void setIndividuallyTest() {

		System.clearProperty("variant.config.resource");
		System.clearProperty("variant.config.file");

		System.setProperty("xyz", "xyz");
		System.setProperty("variant.foo", "xyz");
		
		Config config = ConfigLoader.load("variant.conf", "variant-default.conf");
		assertEquals(3, config.entrySet().size());
		assertEquals("xyz", config.getString("variant.foo"));
		assertEquals("bar", config.getString("variant.bar"));
		assertEquals(8, config.getInt("variant.seven"));
	}
	
}
