package com.variant.server.boot

import com.variant.core.impl.VariantComptime
import com.variant.core.util.VariantStringUtils
import com.variant.core.impl.VariantCore
import net.liftweb.common.Loggable
import com.variant.server.ServerProperties


/**
 * Entry point into the Variant Server. 
 *
object ServerBootOld extends Loggable {
  
   var core: VariantCore = null
   lazy val properties : ServerProperties = new ServerProperties(core)
   
   val bootTime = System.currentTimeMillis()
   
   **
    * External configuration passes the name of the resource properties file here.
    *
   def init(configNamesAsResources: String*) : Unit = {
      core = new VariantCore(configNamesAsResources:_*)
      core.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.1")

		if (logger.isDebugEnabled) {
			LOG.debug("+-- Bootstrapping Variant Server with following application properties: --");
			for (key <- properties) {
				logger.debug("| " + key.propName() + " = " + properties.get(key, java.lang.String) + " : " + properties.getSource(key));
			}
			LOG.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}

   }
}
*/
