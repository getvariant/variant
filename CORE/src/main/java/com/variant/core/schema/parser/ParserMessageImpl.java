package com.variant.core.schema.parser;

import com.variant.core.UserError;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;

/**
 * Parser Message. Generated by Variant parser at run time and obtainable
 * from {@link ParserResponse}.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class ParserMessageImpl implements ParserMessage {
	
	private final Severity severity;
	private final String message;
	private final int code;	
	private Integer line = null, column = null;

	/**
	 * 
	 * @param error
	 * @param args
	 * @since 0.5
	 */
	 public ParserMessageImpl(UserError error, String...args) {
		severity = error.severity;
		message = String.format(error.msgFormat, (Object[]) args);
		code = error.code;
	}

	/**
	 * 
	 * @param error
	 * @param line
	 * @param column
	 * @param args
	 * @since 0.5
	 */
	public ParserMessageImpl(UserError error, int line, int column, String...args) {
		this(error, args);
		this.line = line;
		this.column = column;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 * @since 0.5
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * 
	 * @return
	 * @since 0.5
	 */
	public String getText() {
		return message;
	}
	
	/**
	 * Error code
	 * @return
	 * @since 0.5
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 
	 * @return
	 * @since 0.5
	 */
	public Integer getLine() {
		return line;
	}
	
	/**
	 * 
	 * @return
	 * @since 0.5
	 */
	public Integer getColumn() {
		return column;
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
		result.append(getSeverity().name()).append(" - [").append(getCode()).append("] ").append(getText());
		if (line != null) result.append(String.format(" @ line %s, column %s", line, column));
		return result.toString();
	}

}
