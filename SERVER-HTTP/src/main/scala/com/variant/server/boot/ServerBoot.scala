package com.variant.server.boot

import com.variant.core.impl.VariantComptime
import com.variant.core.util.VariantStringUtils
import com.variant.core.impl.VariantCore

object ServerBoot {
  
   var core: VariantCore = null
   var bootTime = System.currentTimeMillis()
   
   /**
    * External configuration passes the name of the resource properties file here.
    */
   def init(configNamesAsResources: String*) : Unit = {
      val serverConfigNameAsResource = "/variant-server." + VariantStringUtils.RESOURCE_POSTFIX + ".props"
      core = new VariantCore((configNamesAsResources :+ serverConfigNameAsResource):_*)
      core.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.0")
   }
   
}