package com.variant.server.config

//import scala.collection.JavaConverters.mapAsScalaMapConverter
import com.variant.core.config.PropertiesChain
import com.variant.core.Variant

object VariantCore {
  
   private val randomSuffix = "93449EFFB4B5BA0"
   
   val api = Variant.Factory.getInstance();
   
   //api.bootstrap("/variant-server-" + randomSuffix + ".props");
   api.bootstrap();
   
   
}