package com.variant.core.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.VariantCoreInitParams;
import com.variant.core.exception.VariantRuntimeException;

public class VariantCoreInitParamsImpl extends HashMap<String, Object> implements VariantCoreInitParams {
		
	/**
	 */
	private static final long serialVersionUID = 1L;
	private VariantCore core; 
	
	/**
	 * Package construction
	 * @param coreApi
	 * @param map
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("unchecked")
	public VariantCoreInitParamsImpl(VariantCore core, String json) throws Exception {
		this.core = core;
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		super.putAll(jacksonDataMapper.readValue(json, Map.class));
	}
	
	@Override
	public Object getOr(String param, Object defaultValue) {
		Object result = super.get(param);
		return result == null ? defaultValue : result;
	}

	@Override
	public Object getOrThrow(String key, VariantRuntimeException e) {
		Object result = super.get(key);
		if (result == null) throw e;
		else return result;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * The core API that created this object.
	 */
	public VariantCore getCore() {
		return core;
	}
}


