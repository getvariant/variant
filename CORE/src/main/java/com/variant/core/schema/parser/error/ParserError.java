package com.variant.core.schema.parser.error;

import com.variant.core.error.UserError;
import com.variant.core.schema.ParserMessage.Location;

abstract public class ParserError extends UserError {
	
	/**
	 */
	protected ParserError(int code, Severity severity, String format) {
		super(code, severity, format);
	}
	
	/**
	 * Runtime message
	 */
	public String asMessage(Location location, Object...msgArgs) {
		return super.asMessage(msgArgs) + "\nLocation: " + location.getPath();
	}

}
