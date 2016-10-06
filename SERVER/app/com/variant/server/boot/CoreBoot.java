package com.variant.server.boot;

import play.Logger;

import com.variant.core.VariantProperties;
import com.variant.core.VariantCorePropertyKeys.Key;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCore;
import com.variant.server.ServerPropertyKeys;

/**
 * Utility translators from Java.
 * Mostly, because I don't know how to do this in Scala, so this should eventually disappear.
 * 
 * @author Igor.
 *
 */
public class CoreBoot {

	final static Logger.ALogger logger = Logger.of(CoreBoot.class);
	
	public static VariantCore initCore() {
		VariantCore core = new VariantCore();
		core.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.3");
		
		if (Logger.isDebugEnabled()) {
			VariantProperties props = core.getProperties();
			// Touch ServerProperties class to cause its instantiation and hence registration of keys with superclass.
			//ServerProperties.SESSION_STORE_VACUUM_INTERVAL_SECS;
  			logger.debug("+-- Bootstrapping Variant Server with following application properties: --");
  			for (Key key: Key.keys(ServerPropertyKeys.class)) {
  				logger.debug("| " + key.propertyName() + " = " + props.get(key, String.class) + " : " + props.getSource(key));
			}
  			logger.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}
		return core;
	}
}
