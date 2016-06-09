package com.variant.core.impl;

import java.util.HashMap;
import java.util.Map;

import com.variant.core.InitializationParams;
import com.variant.core.exception.VariantRuntimeException;

public class InitializationParamsImpl extends HashMap<String, Object> implements InitializationParams {
		
	/**
	 */
	private static final long serialVersionUID = 1L;
	private VariantCore coreApi; 
	
	/**
	 * Package construction
	 * @param coreApi
	 * @param map
	 */
	InitializationParamsImpl(VariantCore coreApi, Map<String,String> map) {
		super(map);
		this.coreApi = coreApi;
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
	public VariantCore getCoreApi() {
		return coreApi;
	}
}


