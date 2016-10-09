/*
package com.variant.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantCorePropertyKeys.Key;
import com.variant.core.VariantProperties;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCore;

public class ServerBoot {

	public static final long bootTime = System.currentTimeMillis();

	private static final Logger LOG = LoggerFactory.getLogger(ServerBoot.class);
	private static VariantCore core = null;
			   
			   
	/**
	 * No argument because tests will boot up lift and therefore call this the same way as main.
	 * Test overrides have to be done via variant.props
	 *
	public static void boot()  {
		core = new VariantCore();
		core.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.3");
		
		if (LOG.isDebugEnabled()) {
			VariantProperties props = core.getProperties();
			// Touch ServerProperties class to cause it's instantiation and hence registration of keys with superclass.
			//ServerProperties.SESSION_STORE_VACUUM_INTERVAL_SECS;
			LOG.debug("+-- Bootstrapping Variant Server with following application properties: --");
			for (Key key: Key.keys(ServerPropertyKeys.class)) {
				LOG.debug("| " + key.propertyName() + " = " + props.get(key, String.class) + " : " + props.getSource(key));
			}
			LOG.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}
	}
		
	/**
	 * 
	 * @return
	 *
	public static VariantCore getCore() {
		return core;
	}
}
*/