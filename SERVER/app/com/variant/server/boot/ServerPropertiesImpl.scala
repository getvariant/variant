package com.variant.server.boot;

import com.variant.core.exception.RuntimeError.CONFIG_PROPERTY_NOT_SET
import play.api.Configuration
import scala.Option
import com.variant.core.VariantProperties
import com.variant.core.exception.RuntimeErrorException;
import scala.collection.immutable

/**
 * Server side implementation of Variant properties.
 * Wraps Play-provided application configuration.
 * @author Igor
 */
class ServerPropertiesImpl(playConfig: Configuration) extends VariantProperties {
  	
	/**
	 * Actual work
	 * Throw exception if none.
	 * @param key
	 * @return
	 *
    private Object get(VariantProperties.Key key) {
    	// Try system property first.
    	String sysProp = System.getProperty(key.getExternalName());
    	if (sysProp != null) return sysProp;
    	
    	// If nothing, try play config.
    	Option<ConfigObject> fromPlay = playConfig.getObject(key.getExternalName());
    	if (fromPlay.isDefined()) return fromPlay.get().;

    	// If nothing, try the default
    	Object dflt =  key.getDefault();
        if (dflt != null) return dflt;
        
        // Not good.
        throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, key.getExternalName());
    }
*/

   /**
    * 
    */
   override def getString(key: VariantProperties.Key): String = {

      // Try system property first.
    	val sysProp = sys.props(key.getExternalName())
    	if (sysProp != null) return sysProp
    	
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
    override def getInt(key: VariantProperties.Key): Int = {
       
    	// Try system property first.
    	val sysProp = sys.props(key.getExternalName())
    	if (sysProp != null) return sysProp.toInt
    	
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
    override def getLong(key: VariantProperties.Key): Long = {
       
    	// Try system property first.
    	val sysProp = sys.props(key.getExternalName())
    	if (sysProp != null) return sysProp.toLong
    	
    	// If nothing, try play config.
    	val fromPlay = playConfig.getLong(key.getExternalName())
    	if (fromPlay.isDefined) return fromPlay.get
    			
    	// If nothing, try the default
    	val dflt =  key.getDefault
      if (dflt != null) return dflt.asInstanceOf[Long]
        
       // Not good.
       throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, key.getExternalName());
    }

}
