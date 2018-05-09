package com.variant.core.impl;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.variant.core.CoreException;
import com.variant.core.VariantEvent;
import com.variant.core.session.CoreSession;

/**
 * 
 * @author Igor.
 */
abstract public class VariantEventSupport implements VariantEvent {
		
	protected final CoreSession session;
	protected String name;
	protected String value;
	protected Date createDate = new Date();
	protected Map<String, String> params = new HashMap<String, String>();
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	/**
	 */
	public VariantEventSupport(CoreSession session, String name) {
		this.session = session;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Map<String,String> getParameterMap() {
		return params;
	}
	
	@Override
	public Date getCreateDate() {
		return createDate;
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

	protected static final String FIELD_NAME_SID = "sid";
	private static final String FIELD_NAME_KEY = "key";
	private static final String FIELD_NAME_NAME = "name";
	private static final String FIELD_NAME_PARAMS = "params";
	private static final String FIELD_NAME_TIMESTAMP = "ts";
	private static final String FIELD_NAME_VALUE = "value";
	
	/**
	 * Serialize to JSON string.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson() {
		try {
			StringWriter result = new StringWriter(1024);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_SID, session.getId());
			jsonGen.writeNumberField(FIELD_NAME_TIMESTAMP, getCreateDate().getTime());
			jsonGen.writeStringField(FIELD_NAME_NAME, getName());
			jsonGen.writeStringField(FIELD_NAME_VALUE, getValue());
			
			if (!params.isEmpty()) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_PARAMS);
				for (Map.Entry<String,String> e: params.entrySet()) {
					jsonGen.writeStartObject();
					jsonGen.writeStringField(FIELD_NAME_KEY, e.getKey());
					jsonGen.writeStringField(FIELD_NAME_VALUE, e.getValue());
					jsonGen.writeEndObject();
				}
				jsonGen.writeEndArray();
			}
			
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to serialize session", e);
		}
	}
	
	/**
	 * Deserialize from JSON string.
	 * We can't instantiate here, so the caller will instantiate and pass us a shell of an
	 * concrete implemantion object and we'll breath life into it. The SID is already set.
	 * @param json
	 * @return
	 */
	public static <T extends VariantEventSupport> T fromJson(T result, Map<String,?> mappedJson) {
		
		result.name = (String) mappedJson.get(FIELD_NAME_NAME);
		result.value = (String) mappedJson.get(FIELD_NAME_VALUE);
		result.createDate = new Date((Long) mappedJson.get(FIELD_NAME_TIMESTAMP));
		
		@SuppressWarnings("unchecked")
		List<Map<String,String>> params = (List<Map<String,String>>) mappedJson.get(FIELD_NAME_PARAMS);
		if (params != null) {
			for (Map<String,String> p: params) {
				result.params.put(p.get(FIELD_NAME_KEY), p.get(FIELD_NAME_VALUE));
			}
		}
		return result;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return toJson();
	}
}
