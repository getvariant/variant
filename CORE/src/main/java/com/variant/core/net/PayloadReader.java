package com.variant.core.net;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCore;

abstract public class PayloadReader<T> extends Payload {

	private T body;
	private VariantCore core;
	
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

			this.body = deserealizeBody(core, (Map<String,?>)fields.get(FIELD_NAME_BODY));

		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to deserealize payload: [" + payload + "]", e);
		}
	}

	protected abstract T deserealizeBody(VariantCore core, Map<String,?> jsonParseTree);
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
		
	/**
	 * 
	 * @param core
	 */
	public PayloadReader(VariantCore core, String payload) {
		this.core = core;
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
	
	/**
	 * 
	 * @return
	 *
	@SuppressWarnings("unchecked")
	public T getBodyObject(Class<T> clazz) {
		
		// By contract (though not syntactically enforced) T is expected to have a 'deserialize' static method.
		Method staticMethod = VariantReflectUtils.getStaticMethod(clazz, "deserialize");
		if (staticMethod == null) 
			throw new VariantInternalException(String.format("Payloadable class [%s] must implement method 'deserialize'", clazz.getName()));

		// Invoke deserialize(), which should return what we want.
		try {
			return (T) staticMethod.invoke(null, core, body);
		} catch (Exception e) {
			throw new VariantInternalException(
					String.format("Unable to invoke [%s.deserialize(VariantCore, Map<String,?>)]", clazz.getName()));
		}
	}
*/
}
