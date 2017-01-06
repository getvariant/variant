package com.variant.core.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.variant.core.VariantEvent;
import com.variant.core.exception.CoreException;

/**
 * 
 * @author Igor.
 */
abstract public class VariantEventSupport implements VariantEvent {
		
	protected Map<String, String> params = new HashMap<String, String>();
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public Map<String,String> getParameterMap() {
		return params;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param key
	 * @param value
	 *
	public void setParameter(String key, String value) {
		params.put(key, value);
	}

	public String getParameter(String key) {
		return params.get(key);
	}
	*/
	
	//---------------------------------------------------------------------------------------------//
	//                                       Serialization                                          //
	//---------------------------------------------------------------------------------------------//

	private static final String FIELD_NAME_KEY = "key";
	private static final String FIELD_NAME_PARAMS = "params";
	private static final String FIELD_NAME_ID = "sid";
	private static final String FIELD_NAME_TIMESTAMP = "ts";
	private static final String FIELD_NAME_VALUE = "val";
	
	/**
	 * Serialize as JSON.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson(String sid) {
		try {
			StringWriter result = new StringWriter(1024);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_ID, sid);
			jsonGen.writeNumberField(FIELD_NAME_TIMESTAMP, getCreateDate().getTime());
			
			if (!params.isEmpty()) {
				jsonGen.writeObjectFieldStart(FIELD_NAME_PARAMS);
				for (Map.Entry<String, String> e: params.entrySet()) {
					jsonGen.writeStringField(FIELD_NAME_KEY, e.getKey());
					jsonGen.writeStringField(FIELD_NAME_VALUE, e.getValue());
				}
				jsonGen.writeEndObject();
			}
			
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to serialize session", e);
		}
	}
}
