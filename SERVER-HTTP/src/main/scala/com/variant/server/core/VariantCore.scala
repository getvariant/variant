package com.variant.server.core

import com.variant.core.Variant
import com.variant.core.impl.VariantCoreImpl

object VariantCore {
  
   var api: VariantCoreImpl = null
   
   /**
    * External configuration passes the name of the resource properties file here.
    */
   def init(configNamesAsResources: String*) : Unit = {
      api = Variant.Factory.getInstance((configNamesAsResources :+ "/variant-server.props"):_*).asInstanceOf[VariantCoreImpl]
      api.getComptime().registerComponent("Server", "0.6.0");
   }
   
}