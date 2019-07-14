package com.variant.server.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import com.variant.core.util.immutable.ImmutableMap;
import com.variant.server.api.Configuration;
import com.variant.server.boot.ServerMessageLocal;
import com.variant.server.boot.ServerExceptionLocal;

/**
 * Effective server configuration.
 */
public class ConfigurationImpl implements Configuration, ConfigKeys {
 

	private final Config config;
	
	/**
	 * Not for public instantiation.
	 * @param config
	 */
	public ConfigurationImpl(Config config) {
		this.config = config;
	}
	
	/**
	 * String getter. Throws exception if not set or wrong type.
	 * @param key
	 * @return
	 */
	private String getString(String key) {
		try {
			return config.getString(key);
		}
		catch (ConfigException.Missing e) {
			throw new ServerExceptionLocal(ServerMessageLocal.CONFIG_PROPERTY_NOT_SET(), key);			
		}
		catch (ConfigException.WrongType e) {
			throw new ServerExceptionLocal(
					ServerMessageLocal.CONFIG_PROPERTY_WRONG_TYPE(), key, 
					ConfigValueType.STRING, 
					config.getValue(key).valueType());			
		}
	}			

	/**
	 * Get Optional raw config value.
	 * @param key
	 * @return
	 */
	private Optional<ConfigValue> getConfigValue(String key) {
		try {
			return Optional.of(config.getValue(key));
		}
		catch (ConfigException.Missing e) {
			return Optional.empty();			
		}
		catch (ConfigException.WrongType e) {
			throw new ServerExceptionLocal(
					ServerMessageLocal.CONFIG_PROPERTY_WRONG_TYPE(), key, 
					ConfigValueType.OBJECT, 
					config.getValue(key).valueType());			
		}
	}			

	/**
	 * Int getter.
	 * @param key
	 * @return
	 */
	private int getInt(String key) {
		try {
			return config.getInt(key);
		}
		catch (ConfigException.Missing e) {
			throw new ServerExceptionLocal(ServerMessageLocal.CONFIG_PROPERTY_NOT_SET(), key);
		}
		catch (ConfigException.WrongType e) {
			throw new ServerExceptionLocal(
					ServerMessageLocal.CONFIG_PROPERTY_WRONG_TYPE(), key, 
					ConfigValueType.NUMBER, 
					config.getValue(key).valueType());			
		}
	}			

	/*--------------------------------------------------------------------------------*/
	/*                               PUBLIC INTERFACE                                 */
	/*--------------------------------------------------------------------------------*/

	@Override
	public int getHttpPort() {
		return getInt("http.port");
	}

	@Override
	public String getSchemataDir() {
		return getString(SCHEMATA_DIR);
	}

	@Override
	public int getSessionTimeout() {
		return getInt(SESSION_TIMEOUT);
	}

	@Override
	public int getSessionVacuumInterval() {
		return getInt(SESSION_VACUUM_INTERVAL);
	}

	@Override
	public String getDefaultEventFlusherClassName() {
		return getString(EVENT_FLUSHER_CLASS_NAME);
	}

	@Override
	public Optional<String> getDefaultEventFlusherClassInit() {
		return getConfigValue(EVENT_FLUSHER_CLASS_INIT).map( val -> val.render(ConfigRenderOptions.concise()));
	}

	@Override
	public int getEventWriterBufferSize() {
		return getInt(EVENT_WRITER_BUFFER_SIZE);
	}

	@Override
	public int getEventWriterMaxDelay() {
		return getInt(EVENT_WRITER_MAX_DELAY);
	}

	/*--------------------------------------------------------------------------------*/
	/*                                  PUBLIC EXT                                    */
	/*--------------------------------------------------------------------------------*/

	public Map<String, Object> asMap() {
		@SuppressWarnings("serial")
		HashMap<String, Object> result = new HashMap<String, Object>() {{
			put(SCHEMATA_DIR, getString(SCHEMATA_DIR));
			put(SESSION_TIMEOUT, getSessionTimeout());
			put(SESSION_VACUUM_INTERVAL, getSessionVacuumInterval());
			put(EVENT_FLUSHER_CLASS_NAME, getDefaultEventFlusherClassName());	
			put(EVENT_FLUSHER_CLASS_INIT, getDefaultEventFlusherClassInit());
			put(EVENT_WRITER_BUFFER_SIZE, getEventWriterBufferSize());
			put(EVENT_WRITER_MAX_DELAY, getEventWriterMaxDelay());
         put(HTTP_PORT, getHttpPort());
		}};
		return new ImmutableMap<String, Object>(result);
	}

}

					