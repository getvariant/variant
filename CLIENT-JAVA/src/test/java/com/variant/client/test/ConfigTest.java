package com.variant.client.test;

import static com.variant.client.ConfigKeys.SESSION_ID_TRACKER_CLASS_INIT;
import static com.variant.client.ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.ConfigKeys.TARGETING_STABILITY_DAYS;
import static com.variant.client.ConfigKeys.TARGETING_TRACKER_CLASS_INIT;
import static com.variant.client.ConfigKeys.TARGETING_TRACKER_CLASS_NAME;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.typesafe.config.Config;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientError;

public class ConfigTest extends BaseTest {

	@Test
	public void test() throws Exception {
				
		// Regular startup: variant.conf overrides the defaults
		Config config = VariantClient.Factory.getInstance().getConfig();
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("com.variant.client.session.SessionIdTrackerSimple", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(SESSION_ID_TRACKER_CLASS_INIT).entrySet().size());
		assertEquals("com.variant.client.session.TargetingTrackerSimple", config.getString(TARGETING_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(TARGETING_TRACKER_CLASS_INIT).entrySet().size());
		
		// Override with resource
		System.setProperty("variant.config.resource", "variant-ConfigTest1.conf");
		config = VariantClient.Factory.getInstance().getConfig();
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("I'm from variant-ConfigTest1.conf", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(SESSION_ID_TRACKER_CLASS_INIT).entrySet().size());
		assertEquals("no default", config.getString(TARGETING_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(TARGETING_TRACKER_CLASS_INIT).entrySet().size());

		// Override with file and resource - error
		System.setProperty("variant.config.file", "src/test/resources/variant-ConfigTest2.conf");
		new RuntimeErrorExceptionInterceptor() { 
			@Override public void toRun() {
				VariantClient.Factory.getInstance().getConfig();				
			}
		}.assertThrown(ClientError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);


		// Override with just file
		System.clearProperty("variant.config.resource");
		config = VariantClient.Factory.getInstance().getConfig();				
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("I'm from variant-ConfigTest2.conf", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(SESSION_ID_TRACKER_CLASS_INIT).entrySet().size());
		assertEquals("no default", config.getString(TARGETING_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(TARGETING_TRACKER_CLASS_INIT).entrySet().size());

	}

}