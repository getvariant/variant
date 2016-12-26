package com.variant.client.test;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.*;

import com.typesafe.config.Config;

import static com.variant.client.ConfigKeys.*;

import com.variant.client.VariantClient;
import com.variant.client.impl.ClientError;

public class ConfigTest extends BaseTest {

	@Test
	public void test() throws Exception {
				
		// Regular startup: variant.conf overrides the defaults
		Config config = VariantClient.Factory.getInstance().getConfig();
		assertEquals("http://localhost:9000/test", config.getString(SERVER_ENDPOINT_URL));
		assertEquals(0, config.getInt(TARGETING_STABILITY_DAYS));
		assertEquals("com.variant.client.session.SessionIdTrackerSimple", config.getString(SESSION_ID_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(SESSION_ID_TRACKER_CLASS_INIT).entrySet().size());
		assertEquals("com.variant.client.session.TargetingTrackerSimple", config.getString(TARGETING_TRACKER_CLASS_NAME));
		assertEquals(0, config.getObject(TARGETING_TRACKER_CLASS_INIT).entrySet().size());
		
		// Override with resource
		System.setProperty("variant.config.resource", "variant-ConfigTest1.conf");
		config = VariantClient.Factory.getInstance().getConfig();
		assertEquals("I'm from variant-ConfigTest1.conf", config.getString(SERVER_ENDPOINT_URL));

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
		assertEquals("I'm from variant-ConfigTest2.conf", config.getString(SERVER_ENDPOINT_URL));

	}

}
