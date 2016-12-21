package com.variant.core.net;
/*
import java.util.HashMap;

import com.variant.core.exception.RuntimeInternalException;

abstract public class Payload {

	protected HashMap<String,String> propMap = new HashMap<String,String>();
		
	/**
	 * 
	 * @param session
	 *
	protected Payload() {}
	
	protected static final String FIELD_NAME_HEAD = "head";
	protected static final String FIELD_NAME_BODY = "body";

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public enum Property {
		SVR_REL,          // Server release, for handshake with client
		SSN_TIMEOUT       // Session timeout, seconds.
	}

	/**
	 * 
	 * @param prop
	 * @param value
	 *
	public void setProperty(Property prop, String value) {
		propMap.put(prop.toString(), value);
	}

	/**
	 * 
	 * @param prop
	 * @return
	 *
	public String getProperty(Property prop) {
		return propMap.get(prop.toString());
	}
	
	/**
	 * 
	 *
	@SuppressWarnings("unchecked")
	public <T> T getProperty(Property prop, Class<T> clazz) {
		if (clazz == String.class)
			return (T) getProperty(prop);
		else if (clazz == Integer.class)
			return (T) new Integer(getProperty(prop));
		else if (clazz == Long.class)
			return (T) new Long(getProperty(prop));
		else 
			throw new RuntimeInternalException("Unable to convert property value to instance of [" + clazz.getName() + "]");
	}

}
*/