package com.variant.server.config

//import scala.collection.JavaConverters.mapAsScalaMapConverter
import com.variant.core.config.PropertiesChain
import com.variant.core.Variant

object VariantCore {
  
   private val randomSuffix = "93449EFFB4B5BA0"
   private val core = Variant.Factory.getInstance();
   core.bootstrap("/variant-server-" + randomSuffix + ".props");
   
   
}