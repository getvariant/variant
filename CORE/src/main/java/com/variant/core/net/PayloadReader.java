package com.variant.core.net;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.VariantInternalException;

public class PayloadReader extends Payload {

	private Map<String, ?> body;
	
	/**
	 * 
	 * @param buffer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void parse(String payload) {
		try {
			ObjectMapper mapper = new ObjectMapper();		
			Map<String,?> fields = mapper.readValue(payload, Map.class);

			Map<String,?> head = (Map<String,?>) fields.get(FIELD_NAME_HEAD);
			for (Map.Entry<String, ?> e: head.entrySet()) {
				propMap.put(e.getKey(), (String)e.getValue());
			}

			this.body = (Map<String,?>) fields.get(FIELD_NAME_BODY);

		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to deserealize payload: [" + payload + "]", e);
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
		
	/**
	 * 
	 * @param core
	 */
	public PayloadReader(String payload) {
		parse(payload);
	}
				
	/**
	 * 
	 * @return
	 */
	public Map<String,?> getBody() {
		return body;
	}
		
}
