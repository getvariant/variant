package com.variant.core.event.impl;

import java.util.HashMap;
import java.util.Map;

import com.variant.core.event.VariantEvent;

/**
 * 
 * @author Igor.
 */
abstract public class VariantEventSupport implements VariantEvent {
		
	protected Map<String, Object> params = new HashMap<String, Object>();
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public Map<String,Object> getParameterMap() {
		return params;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void setParameter(String key, Object value) {
		params.put(key, value);
	}

	public Object getParameter(String key) {
		return params.get(key);
	}

}
