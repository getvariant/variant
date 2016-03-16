package com.variant.ws.server.core

//import scala.collection.JavaConverters.mapAsScalaMapConverter
import com.variant.core.config.PropertiesChain
import com.variant.core.Variant
import scala.io.Source
import com.variant.core.impl.VariantCoreImpl

object VariantCore {
  
   var api: VariantCoreImpl = null
   
   /**
    * External configuration passes the name of the resource properties file here.
    */
   def init(configNamesAsResources: String*) : Unit = {
      api = Variant.Factory.getInstance("/variant-server.props" +: configNamesAsResources:_*).asInstanceOf[VariantCoreImpl]
   }
   
}