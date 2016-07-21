package com.variant.core.net;

import java.io.StringWriter;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.exception.VariantInternalException;

public class PayloadWriter extends Payload {

	private String body;
	
	/**
	 * 
	 * @return
	 */
	private String toJson() {
		try {
			StringWriter result = new StringWriter(4096);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();

			jsonGen.writeObjectFieldStart(FIELD_NAME_HEAD);
			for (Map.Entry<String, String> e: propMap.entrySet()) {
				jsonGen.writeStringField(e.getKey(), e.getValue());
			}
			jsonGen.writeEndObject();
			jsonGen.writeFieldName(FIELD_NAME_BODY);
			jsonGen.writeRawValue(body);

			
			jsonGen.writeEndObject();

			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to serialize session", e);
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public PayloadWriter(String body) {
		this.body = body;
	}

	/**
	 */
	public PayloadWriter(byte[] body) {
		this.body = new String(body);
	}
		
	/**
	 */
	public String getAsJson() {
		return toJson();
	}
		
}
