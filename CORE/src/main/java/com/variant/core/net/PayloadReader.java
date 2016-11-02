package com.variant.core.net;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.xdm.Schema;

abstract public class PayloadReader<T> extends Payload {

	private T body;
	private Schema schema;
	
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

			this.body = deserealizeBody(schema, (Map<String,?>)fields.get(FIELD_NAME_BODY));

		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to deserealize payload: [" + payload + "]", e);
		}
	}

	protected abstract T deserealizeBody(Schema schema, Map<String,?> mappedJson);
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
		
	/**
	 * 
	 * @param core
	 */
	public PayloadReader(Schema schema, String payload) {
		this.schema = schema;
		parse(payload);
	}
	
	/**
	 * 
	 * @return
	 */
	public T getBody() {
		return body;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return body == null;
	}
	
}
