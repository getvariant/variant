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
    	Option<String> result = playConfig.getString(key.getExternalName(), scala.Option.apply(null));
    	return (result.isEmpty() || result.get().isEmpty()) ? key.getDefault() : result.get();
    }

    @Override
    public String getString(VariantProperties.Key key) {
    	return get(key).toString();
    }
    
    @Override
    public long getLong(VariantProperties.Key key) {
    	return Long.parseLong(getString(key));
    }

}
