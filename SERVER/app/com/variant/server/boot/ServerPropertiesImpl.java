package com.variant.server.boot;

import play.api.Configuration;
import scala.Option;

import com.variant.core.VariantProperties;
import com.variant.core.exception.RuntimeErrorException;
import static com.variant.core.exception.RuntimeError.CONFIG_PROPERTY_NOT_SET;
import com.variant.server.ServerPropertiesKey;

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
    	Object result = get(key);
        if (result == null)
        	throw new RuntimeErrorException(CONFIG_PROPERTY_NOT_SET, ServerPropertiesKey.SCHEMAS_DIR.getExternalName());
        return result.toString();
    }
    
    @Override
    public int getInt(VariantProperties.Key key) {
    	return Integer.parseInt(getString(key));
    }

    @Override
    public long getLong(VariantProperties.Key key) {
    	return Long.parseLong(getString(key));
    }

}
