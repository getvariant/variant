package com.variant.client.net;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.InternalErrorException;
import com.variant.core.VariantException;

abstract public class AbstractPayloadReader<T>  {

	private T content = null;
	
	/**
	 * 
	 * @param buffer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void parseJson(String payload) {
		try {
			ObjectMapper mapper = new ObjectMapper();		
			this.content = (T) parser().parse(mapper.readValue(payload, Map.class));
		}
		catch (VariantException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InternalErrorException("Unable to deserealize payload: [" + payload + "]", e);
		}
	}
	
	abstract protected AbstractParser<T> parser();
	
	/**
	 * Implementations will differ at construction but have an expected parse() signature.
	 */
	protected interface AbstractParser<T> {
		T parse(Map<String,?> mappedJson);
	}

	/**
	 * 
	 */
	protected AbstractPayloadReader(String payload) {
		parseJson(payload);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
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
