package com.variant.server.core

import com.variant.core.Variant
import com.variant.core.impl.VariantCoreImpl
import com.variant.core.util.VariantStringUtils
import com.variant.core.impl.VariantComptime

object VariantCore {
  
   var api: VariantCoreImpl = null
   
   /**
    * External configuration passes the name of the resource properties file here.
    */
   def init(configNamesAsResources: String*) : Unit = {
      val serverConfigNameAsResource = "/variant-server." + VariantStringUtils.RESOURCE_POSTFIX + ".props"
      api = Variant.Factory.getInstance((configNamesAsResources :+ serverConfigNameAsResource):_*).asInstanceOf[VariantCoreImpl]
      api.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.0")
   }
   
}