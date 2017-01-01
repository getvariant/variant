package com.variant.client.net;
/*
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.InternalErrorException;
import com.variant.core.VariantException;

public class PayloadReader<T extends Payload> {

	private T content = null;
		
	/**
	 * 
	 *
	@SuppressWarnings("unchecked")
	public PayloadReader(String raw, Class<T> cls) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			if (cls.isInstance(Payload.Connection.class)) {
				content = (T) Payload.Connection.parse(mapper.readValue(raw, Map.class));
				
			}
			else {
				throw new VariantException(String.format("Don't know how to parse payload type [%s]", cls.getName()));
			}
		}
		catch (VariantException e) {
			throw e;
		}
		catch (Throwable t) {
			throw new VariantException(String.format("Unable to parse payload of type [%s]", cls.getName()), t);
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	/**
	 * 
	 * @return
	 *
	public T getContent() {
		return content;
	}
	
	/**
	 * 
	 * @return
	 *
	public boolean isEmpty() {
		return content == null;
	}
}
*/