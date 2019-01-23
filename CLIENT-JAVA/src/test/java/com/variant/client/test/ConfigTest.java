package com.variant.client.test;

import static com.variant.client.impl.ConfigKeys.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.test.util.ClientBaseTest;

public class ConfigTest extends ClientBaseTest {

	@Test
	public void alternateConfigFileTest() throws Exception {
				
		System.clearProperty(TARGETING_STABILITY_DAYS);

		// Regular startup: variant.conf overrides the defaults
		Config config = VariantClient.Factory.getInstance().getConfig();
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("com.variant.client.session.SessionIdTrackerSimple", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals("com.variant.client.session.TargetingTrackerSimple", config.getString(TARGETING_TRACKER_CLASS_NAME));
		
		// Override with resource
		System.setProperty("variant.config.resource", "variant-ConfigTest1.conf");
		config = VariantClient.Factory.getInstance().getConfig();
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("I'm from variant-ConfigTest1.conf", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals("com.variant.client.session.TargetingTrackerSimple", config.getString(TARGETING_TRACKER_CLASS_NAME));

		// Override with file and resource - error
		System.setProperty("variant.config.file", "src/test/resources/variant-ConfigTest2.conf");
		new CoreUserExceptionInterceptor() { 
			@Override public void toRun() {
				VariantClient.Factory.getInstance().getConfig();				
			}
		}.assertThrown(ClientUserError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);


		// Override with just file
		System.clearProperty("variant.config.resource");
		config = VariantClient.Factory.getInstance().getConfig();				
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("I'm from variant-ConfigTest2.conf", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals("com.variant.client.session.TargetingTrackerSimple", config.getString(TARGETING_TRACKER_CLASS_NAME));
	}

	@Test
	public void propOnCommandLineOverrideTest() throws Exception {

		System.clearProperty("variant.config.resource");
		System.clearProperty("variant.config.file");
		System.setProperty(TARGETING_STABILITY_DAYS, "456");
		Config config = VariantClient.Factory.getInstance().getConfig();
		assertEquals(456L, config.getNumber(TARGETING_STABILITY_DAYS));
	}
}
