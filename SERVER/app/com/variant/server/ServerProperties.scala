package com.variant.server

import com.variant.core.exception.RuntimeError.CONFIG_PROPERTY_NOT_SET
import play.api.Configuration
import com.variant.core.exception.RuntimeErrorException
import scala.collection.immutable
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigObject

/**
 * Server side implementation of Variant properties.
 * Wraps Play-provided application configuration.
 * @author Igor
 */
class ServerProperties(playConfig: Configuration) {
  	
   /**
    * 
    */
   def getString(key: ServerPropertiesKey): String = {

      // Try system property first.
    	// val sysProp = sys.props(key.getExternalName())
    	// if (sysProp != null) return sysProp
    	
    	// If nothing, try play config.
    	val fromPlay = playConfig.getString(key.getExternalName(), Option(immutable.Set.empty[String]))
    	if (fromPlay.isDefined) return fromPlay.get
    			
    	// If nothing, try the default
    	val dflt =  key.getDefault
      if (dflt != null) return dflt.asInstanceOf[String]
        
       // Not good.
       throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, key.getExternalName());
    }
    
   /**
    * 
    */
    def getInt(key: ServerPropertiesKey): Int = {
       
    	// Try system property first.
    	//val sysProp = sys.props(key.getExternalName())
    	//if (sysProp != null) return sysProp.toInt
    	
    	// If nothing, try play config.
    	val fromPlay = playConfig.getInt(key.getExternalName())
    	if (fromPlay.isDefined) return fromPlay.get
    			
    	// If nothing, try the default
    	val dflt =  key.getDefault
      if (dflt != null) return dflt.asInstanceOf[Int]
        
       // Not good.
       throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, key.getExternalName());
    }

   /**
    * 
    */    
    def getLong(key: ServerPropertiesKey): Long = {
       
    	// Try system property first.
    	//val sysProp = sys.props(key.getExternalName())
    	//if (sysProp != null) return sysProp.toLong
    	
    	// If nothing, try play config.
    	val fromPlay = playConfig.getLong(key.getExternalName())
    	if (fromPlay.isDefined) return fromPlay.get
    			
    	// If nothing, try the default
    	val dflt =  key.getDefault
      if (dflt != null) return dflt.asInstanceOf[Long]
        
       // Not good.
       throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, key.getExternalName());
    }

   /**
    * 
    */    
    def getObject(key: ServerPropertiesKey): ConfigObject = {
       
    	// Try system property first.
    	//val sysProp = sys.props(key.getExternalName())
    	//if (sysProp != null) return sysProp.toLong
    	
    	// If nothing, try play config.
    	val fromPlay = playConfig.getObject(key.getExternalName())
    	if (fromPlay.isDefined) return fromPlay.get
    			
    	// If nothing, try the default
    	//val dflt =  key.getDefault
      //if (dflt != null) return dflt.asInstanceOf[Long]
        
       // Not good.
       throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, key.getExternalName());
    }

}
