package com.variant.core.schema.parser;


/**
 * Parser Message.
 * 
 * @author Igor
 */
public class ParserMessage {
	
	private Severity severity;
	private String message;
	private String code;	
	private Integer line = null, column = null;

	/**
	 * 
	 * @param template
	 * @param args
	 */
	public ParserMessage(MessageTemplate template, String...args) {
		severity = template.getSeverity();
		message = String.format(template.getFormat(), (Object[]) args);
		code = template.toString();
	}

	/**
	 * 
	 * @param template
	 * @param line
	 * @param column
	 * @param args
	 */
	public ParserMessage(MessageTemplate template, int line, int column, String...args) {
		this(template, args);
		this.line = line;
		this.column = column;
	}

	/**
	 * User defined errors do not use templates.
	 * @param template
	 * @param args
	 */
	public ParserMessage(Severity severity, String message) {
		this.severity = severity;
		this.message = message;
		code = "USER_DEFINED_MESSAGE";
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Error code
	 * @return
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getLine() {
		return line;
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getColumn() {
		return column;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getSeverity().name()).append(" - [").append(getCode()).append("] ").append(getMessage());
		if (line != null) result.append(String.format(" @ line %s, column %s", line, column));
		return result.toString();
	}

}
