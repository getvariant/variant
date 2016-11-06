package com.variant.server.boot;

import play.api.Configuration;
import scala.Option;

import com.variant.core.VariantProperties;

/**
 * Server side implementation of Variant properties.
 * Wraps Play-provided application configuration.
 * @author Igor
 */
class ServerPropertiesImpl implements VariantProperties {
  
	private final Configuration playConfig;
	
	public ServerPropertiesImpl(Configuration playConfig) {
		this.playConfig = playConfig;
	}
	
    private Object get(VariantProperties.Key key) {
    	Option<String> result = playConfig.getString(key.name, scala.Option.apply(null));
    	return (result.isEmpty() || result.get().isEmpty()) ? key.getDefaultValue : result.get();
    }

    @Override
    public String getString(VariantProperties.Key key) {
    	return get(key);
    }
    
    @Override
    public long getLong(VariantProperties.Key key) {
    	return Long.parseLong(get(key));
    }

}
