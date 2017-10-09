package com.variant.core.schema.parser;

import com.variant.core.UserError;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;

/**
 * ParserMessage implementation.
 * 
 * @author Igor Urisman
 */
public class ParserMessageImpl implements ParserMessage {
	
	private final Severity severity;
	private final String message;
	private final int code;	
	private Location location = null;

	/**
	 */
	 public ParserMessageImpl(UserError error, String...args) {
		severity = error.getSeverity();
		message = error.asMessage((Object[])args);
		code = error.getCode();
	}

	/**
	 */
	public ParserMessageImpl(Location location, UserError error, String...args) {
		this(error, args);
		this.location = location;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	@Override
	public Severity getSeverity() {
		return severity;
	}

	/**
	 */
	@Override
	public String getText() {
		return message;
	}
	
	/**
	 */
	@Override
	public int getCode() {
		return code;
	}
	
	/**
	 */
	@Override
	public Location getLocation() {
		return location;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                         PUBLIC EXT                                          //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[").append(getCode()).append("] [").append(getSeverity().name()).append("] ").append(getText());
		if (location != null) result.append("\nLocation: ").append(location);
		return result.toString();
	}

}
