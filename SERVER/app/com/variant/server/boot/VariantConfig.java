package com.variant.server.boot;

import play.api.Configuration;
import scala.Option;

/**
 * Wrap Play provided applicaiton configuration in 
 * Variant specific adaptor.
 */
public class VariantConfig {
  
	private final Configuration playConfig;
	
	VariantConfig(Configuration playConfig) {
		this.playConfig = playConfig;
	}
	
    private String get(VariantConfigKey key) {
    	Option<String> result = playConfig.getString(key.name, scala.Option.apply(null));
    	return (result.isEmpty() || result.get().isEmpty()) ? key.defaultValue.toString() : result.get();
    }

    public String getString(VariantConfigKey key) {
    	return get(key);
    }
    
    public int getInt(VariantConfigKey key) {
    	return Integer.parseInt(get(key));
    }

    public long getLong(VariantConfigKey key) {
    	return Long.parseLong(get(key));
    }

}
