package com.variant.client.net;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Schema;

abstract public class PayloadReader<T>  {

	private T content = null;
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
			this.content = (T) parse(schema, mapper.readValue(payload, Map.class));
		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to deserealize payload: [" + payload + "]", e);
		}
	}

	protected abstract T parse(Schema schema, Map<String,?> mappedJson);
	
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
	public T getContent() {
		return content;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return content == null;
	}
	
}
