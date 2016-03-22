package com.variant.ws.server.core

//import scala.collection.JavaConverters.mapAsScalaMapConverter
import com.variant.core.config.PropertiesChain
import com.variant.core.Variant
import scala.io.Source
import com.variant.core.impl.VariantCoreImpl
import com.variant.core.config.ComptimeService

object VariantCore {
  
   var api: VariantCoreImpl = null
   
   /**
    * External configuration passes the name of the resource properties file here.
    */
   def init(configNamesAsResources: String*) : Unit = {
      ComptimeService.registerComponent("Server", "0.6.0");
      api = Variant.Factory.getInstance((configNamesAsResources :+ "/variant-server.props"):_*).asInstanceOf[VariantCoreImpl]
   }
   
}